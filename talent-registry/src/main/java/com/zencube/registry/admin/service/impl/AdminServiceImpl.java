package com.zencube.registry.admin.service.impl;

import com.zencube.registry.admin.dto.request.AssignRoleRequest;
import com.zencube.registry.admin.dto.request.CreateRoleRequest;
import com.zencube.registry.admin.dto.request.RemoveRoleRequest;
import com.zencube.registry.admin.dto.response.RoleDetailsResponse;
import com.zencube.registry.admin.dto.response.RoleResponse;
import com.zencube.registry.admin.dto.response.UserRoleResponse;
import com.zencube.registry.admin.service.AdminService;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.RoleRepository;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.enums.RoleType;
import com.zencube.registry.common.exception.*;
import com.zencube.registry.rolepermission.entity.RolePermission;
import com.zencube.registry.userrole.entity.UserRole;
import com.zencube.registry.userrole.repository.UserRoleRepository;
import com.zencube.registry.rolepermission.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public void assignRole(AssignRoleRequest request) {
        User user = userRepository.findById(request.userId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Role role = roleRepository.findById(request.roleId())
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + request.roleId()));

        if (userRoleRepository.existsByUserAndRoleAndDeletedFalse(user, role)) {
            throw new RoleAlreadyAssignedException("User already has the role: " + role.getName());
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        
        log.info("Assigned role {} to user {}", role.getName(), user.getEmail());
    }

    @Override
    @Transactional
    public void removeRole(RemoveRoleRequest request) {
        User user = userRepository.findById(request.userId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Role role = roleRepository.findById(request.roleId())
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + request.roleId()));

        UserRole userRole = userRoleRepository.findByUserAndRoleAndDeletedFalse(user, role)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have the role: " + role.getName()));

        userRole.softDelete("ADMIN_USER"); // Normally would pass current auditor username
        userRoleRepository.save(userRole);
        
        log.info("Removed role {} from user {}", role.getName(), user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findByDeletedFalse().stream()
                .map(r -> new RoleResponse(r.getId(), r.getName(), r.getDescription(), r.isSystem(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailsResponse getRoleDetails(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));

        List<String> permissions = rolePermissionRepository.findByRoleAndDeletedFalse(role).stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toList());

        return new RoleDetailsResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                permissions,
                role.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCaseAndDeletedFalse(request.name().trim())) {
            throw new DuplicateRoleException("Role already exists with name: " + request.name().trim());
        }

        Role role = Role.builder()
                .name(request.name().trim())
                .description(request.description())
                .roleType(RoleType.CUSTOM)
                .isSystem(false)
                .build();

        role = roleRepository.save(role);
        log.info("Created custom role: {}", role.getName());
        
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), role.isSystem(), role.getCreatedAt());
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));

        if (role.isSystem()) {
            throw new SystemRoleModificationException("Cannot delete a builtin system role.");
        }

        // Soft delete all user-role mappings
        List<UserRole> userRoles = userRoleRepository.findByRoleAndDeletedFalse(role);
        for (UserRole ur : userRoles) {
            ur.softDelete("ADMIN_USER");
        }
        userRoleRepository.saveAll(userRoles);

        // Soft delete all role-permission mappings
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleAndDeletedFalse(role);
        for (RolePermission rp : rolePermissions) {
            rp.softDelete("ADMIN_USER");
        }
        rolePermissionRepository.saveAll(rolePermissions);

        // Soft delete the role
        role.softDelete("ADMIN_USER");
        roleRepository.save(role);
        
        log.info("Deleted custom role: {}", role.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRoleResponse> listUsersByRole(String roleName) {
        List<UserRole> userRoles = userRoleRepository.findByRoleNameAndDeletedFalse(roleName);
        return userRoles.stream()
                .map(ur -> new UserRoleResponse(
                        ur.getUser().getId(),
                        ur.getUser().getEmail(),
                        ur.getUser().getStatus().name(),
                        ur.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
