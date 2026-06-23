package com.zencube.registry.user.mapper;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.enums.UserStatus;
import com.zencube.registry.user.dto.CreateUserRequest;
import com.zencube.registry.user.dto.UserAdminResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manual mapper between the {@link User} JPA entity and the User Management
 * module's DTOs ({@link CreateUserRequest} and {@link UserAdminResponse}).
 *
 * <h2>Coexistence with AuthMapper</h2>
 * <p>{@link com.zencube.registry.auth.mapper.AuthMapper} handles entity↔DTO
 * conversions for the <em>authentication flow</em> (register/login).
 * This mapper handles conversions for the <em>admin user management flow</em>.
 * Both operate on the same {@link User} entity but produce different DTOs:
 * <ul>
 *   <li>{@code AuthMapper} → {@code com.zencube.registry.auth.dto.UserResponse} (record)</li>
 *   <li>{@code UserMapper} → {@link UserAdminResponse} (Lombok builder class)</li>
 * </ul>
 * This separation keeps each module's concerns isolated and avoids coupling
 * the auth module to admin-specific fields like {@code updatedAt}.
 *
 * <h2>Why No Constructor for UserRepository?</h2>
 * <p>The mapper has no repository dependency. Password encoding is handled
 * exclusively in {@link com.zencube.registry.user.service.impl.UserServiceImpl}
 * which has the {@link org.springframework.security.crypto.password.PasswordEncoder}
 * injected. The mapper's job is pure structural transformation only.
 */
@Component
public class UserMapper {

    // ------------------------------------------------------------------
    // CreateUserRequest → User (new entity, not yet persisted)
    // ------------------------------------------------------------------

    /**
     * Builds a new un-persisted {@link User} entity from a
     * {@link CreateUserRequest}.
     *
     * <p><strong>Important:</strong> The password hash is set separately by
     * the service layer after calling this method. The returned entity has
     * no password hash set.
     *
     * @param request the creation payload
     * @return a transient {@link User} entity ready to be saved
     */
    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .authProvider(request.getAuthProvider() != null
                        ? request.getAuthProvider()
                        : AuthProvider.LOCAL)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();
    }

    // ------------------------------------------------------------------
    // User → UserAdminResponse
    // ------------------------------------------------------------------

    /**
     * Maps a {@link User} entity to a {@link UserAdminResponse} DTO.
     *
     * <p>Sensitive fields ({@code passwordHash}, {@code providerId}) are
     * deliberately omitted.
     *
     * @param user the entity to map; must not be {@code null}
     * @return the read-only admin response DTO
     */
    public UserAdminResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .authProvider(user.getAuthProvider())
                .emailVerified(user.isEmailVerified())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // ------------------------------------------------------------------
    // List<User> → List<UserAdminResponse>
    // ------------------------------------------------------------------

    /**
     * Maps a list of {@link User} entities to a list of
     * {@link UserAdminResponse} DTOs.
     *
     * @param users the entities to map; must not be {@code null}
     * @return list of DTOs in the same order as the input
     */
    public List<UserAdminResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .toList();
    }
}
