package com.zencube.registry.auth.mapper;

import com.zencube.registry.auth.dto.RegisterRequest;
import com.zencube.registry.auth.dto.UserResponse;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.common.enums.UserStatus;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link User} JPA entities and auth-layer DTOs.
 *
 * <p>Deliberately hand-written (no MapStruct) to keep the auth package
 * self-contained and avoid annotation-processor complexity during the
 * early project phase. Migrate to MapStruct if the mapping logic grows.
 */
@Component
public class AuthMapper {

    // ------------------------------------------------------------------
    // RegisterRequest  →  User (new entity, not yet persisted)
    // ------------------------------------------------------------------

    /**
     * Creates a new {@link User} entity from a registration request.
     * The password hash must be set separately by the service layer
     * <em>after</em> calling this method.
     *
     * @param request the inbound registration payload
     * @return an un-persisted {@link User} with status PENDING_VERIFICATION
     */
    public User toUser(RegisterRequest request) {
        return User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email().toLowerCase().trim())
                .status(UserStatus.PENDING_VERIFICATION)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .build();
    }

    // ------------------------------------------------------------------
    // User  →  UserResponse
    // ------------------------------------------------------------------

    /**
     * Maps a {@link User} entity to a {@link UserResponse} DTO,
     * omitting all sensitive fields (password hash, provider ID, etc.).
     *
     * @param user the entity to map
     * @return a safe, public-facing projection
     */
    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDisplayName(),
                user.getPhone(),
                user.getStatus(),
                user.getAuthProvider(),
                user.isEmailVerified(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
