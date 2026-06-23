package com.zencube.registry.user.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.user.dto.CreateUserRequest;
import com.zencube.registry.user.dto.UpdateUserRequest;
import com.zencube.registry.user.dto.UserAdminResponse;
import com.zencube.registry.user.mapper.UserMapper;
import com.zencube.registry.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link UserService} for admin-facing user management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserAdminResponse createUser(CreateUserRequest request) {
        log.info("Admin creating user account for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User", "email", request.getEmail());
        }

        User user = userMapper.toEntity(request);

        // Encode password if provided (it is optional for OAuth2 users)
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        log.info("Successfully created user account with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminResponse getUser(UUID id) {
        log.debug("Fetching user with id: {}", id);
        User user = findUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminResponse getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAdminResponse> getAllUsers() {
        log.debug("Fetching all active users");
        // We only want active (non-deleted) users.
        // Assuming findByDeletedFalse() isn't explicitly defined but the entity
        // is usually handled via soft-delete queries. We will use findAll() and filter
        // or rely on a custom query if added. Let's just use findAll() and filter for now
        // since JpaRepository doesn't automatically filter soft deletes unless Hibernate @Where is used.
        // The UserRepository javadoc says "exclude soft-deleted by default" but JpaRepository findAll()
        // will fetch all unless there's a @SQLRestriction on the entity.
        // Since User extends BaseEntity which has isDeleted, we should ideally fetch correctly.
        // Let's manually filter to be safe if no specific method exists.
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .toList();
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user account with id: {}", id);

        User user = findUserOrThrow(id);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        user = userRepository.save(user);
        log.info("Successfully updated user account with id: {}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Soft-deleting user account with id: {}", id);

        User user = findUserOrThrow(id);
        
        // Soft delete logic is typically handled by setting isDeleted=true 
        // and optionally setting deletedAt and deletedBy.
        user.setDeleted(true);
        user.setDeletedAt(java.time.Instant.now());
        // deletedBy could be populated from SecurityContext if available.
        user.setDeletedBy("system"); 
        
        userRepository.save(user);
        log.info("Successfully soft-deleted user account with id: {}", id);
    }

    /**
     * Helper to find an active user or throw a 404.
     */
    private User findUserOrThrow(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return user;
    }
}
