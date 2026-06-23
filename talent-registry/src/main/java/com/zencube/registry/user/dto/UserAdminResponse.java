package com.zencube.registry.user.dto;

import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only projection of a {@link com.zencube.registry.auth.entity.User} entity
 * returned by the User Management API.
 *
 * <h2>Security — Excluded Fields</h2>
 * <p>The following sensitive fields are <strong>never</strong> exposed:
 * <ul>
 *   <li>{@code passwordHash} — the BCrypt hash</li>
 *   <li>{@code providerId} — the OAuth2 provider's internal user ID</li>
 *   <li>{@code deletedAt}, {@code deletedBy} — internal audit fields</li>
 *   <li>{@code version} — optimistic locking counter</li>
 * </ul>
 *
 * <h2>Relationship to AuthMapper's UserResponse</h2>
 * <p>{@code com.zencube.registry.auth.dto.UserResponse} (a Java record) is used
 * exclusively by the Auth module (register/login flows). This class is the
 * User Management module's own response shape — it includes the same fields
 * plus {@code lastLoginAt} and {@code updatedAt} which are operationally useful
 * for admins.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Full user account representation returned by the User Management API")
public class UserAdminResponse {

    @Schema(description = "User's UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Primary email address", example = "jane.doe@example.com")
    private String email;

    @Schema(description = "First name", example = "Jane")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full display name", example = "Jane Doe")
    private String displayName;

    @Schema(description = "Phone number", example = "+91 9876543210")
    private String phone;

    @Schema(description = "Account lifecycle status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Identity provider", example = "LOCAL")
    private AuthProvider authProvider;

    @Schema(description = "Whether the email has been verified", example = "true")
    private boolean emailVerified;

    @Schema(description = "URL to profile picture")
    private String avatarUrl;

    @Schema(description = "UTC timestamp when the account was created")
    private Instant createdAt;

    @Schema(description = "UTC timestamp of the last account update")
    private Instant updatedAt;
}
