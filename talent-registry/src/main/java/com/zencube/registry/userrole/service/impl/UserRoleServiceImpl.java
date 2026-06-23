package com.zencube.registry.userrole.service.impl;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.userrole.dto.CreateUserRoleRequest;
import com.zencube.registry.userrole.dto.UserRoleResponse;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.mapper.UserRoleMapper;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.userrole.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 1. Purpose
 * Implementation of UserRoleService enforcing assignment business rules.
 *
 * 2. Layer
 * Service Implementation Layer.
 *
 * 4. Annotation Explanation
 * @Service: Marks this as a Spring Service component.
 * @RequiredArgsConstructor: Generates constructor for all `final` fields (constructor injection).
 * @Transactional: Ensures operations run within a DB transaction.
 * @Slf4j: Injects a logger.
 *
 * 5. Business Logic Explanation
 * - Verifies that both the User and the Role exist and are active.
 * - Prevents duplicate assignments (ConflictException).
 * - Soft deletes mappings to maintain an audit history.
 *
 * 6. Best Practices
 * - Strict Constructor Injection used everywhere.
 * - Transactions are read-only when retrieving data to optimize database performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public UserRoleResponse assignRoleToUser(CreateUserRoleRequest request) {
        log.info("Assigning role {} to user {}", request.getRoleId(), request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Role role = roleRepository.findById(request.getRoleId())
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        if (userRoleRepository.existsByUserAndRoleAndDeletedFalse(user, role)) {
            throw new ConflictException("Role '" + role.getName() + "' is already assigned to user '" + user.getEmail() + "'");
        }

        UserRole mapping = UserRole.builder()
                .user(user)
                .role(role)
                .build();

        mapping = userRoleRepository.save(mapping);
        log.info("Successfully assigned role. Mapping ID: {}", mapping.getId());

        return userRoleMapper.toResponse(mapping);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getRolesByUser(UUID userId) {
        log.debug("Fetching roles for user {}", userId);

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<UserRole> mappings = userRoleRepository.findByUserAndDeletedFalse(user);
        return userRoleMapper.toResponseList(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getUsersByRole(UUID roleId) {
        log.debug("Fetching users with role {}", roleId);

        Role role = roleRepository.findById(roleId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        List<UserRole> mappings = userRoleRepository.findByRoleAndDeletedFalse(role);
        return userRoleMapper.toResponseList(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> getAllMappings() {
        log.debug("Fetching all active user-role mappings");
        List<UserRole> mappings = userRoleRepository.findAllActiveWithDetails();
        return userRoleMapper.toResponseList(mappings);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(UUID id) {
        log.info("Revoking user-role mapping {}", id);

        UserRole mapping = userRoleRepository.findById(id)
                .filter(ur -> !ur.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("UserRole", "id", id));

        mapping.setDeleted(true);
        mapping.setDeletedAt(java.time.Instant.now());
        mapping.setDeletedBy("system");

        userRoleRepository.save(mapping);
        log.info("Successfully revoked user-role mapping {}", id);
    }
}
