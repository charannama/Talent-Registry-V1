package com.zencube.registry.auth.dto;

import com.zencube.registry.auth.enums.AuthProvider;
import com.zencube.registry.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only projection of a {@code User} entity returned to API consumers.
 * Password hashes and internal fields are never exposed.
 */
@Schema(description = "Public-facing user profile snapshot")
public record UserResponse(

    @Schema(description = "User's UUID")
    UUID id,

    @Schema(description = "Email address", example = "jane.doe@example.com")
    String email,

    @Schema(description = "First name", example = "Jane")
    String firstName,

    @Schema(description = "Last name", example = "Doe")
    String lastName,

    @Schema(description = "Full display name", example = "Jane Doe")
    String displayName,

    @Schema(description = "Phone number", example = "+1234567890")
    String phone,

    @Schema(description = "Account status")
    UserStatus status,

    @Schema(description = "Identity provider", example = "LOCAL")
    AuthProvider authProvider,

    @Schema(description = "Whether the email has been verified")
    boolean emailVerified,

    @Schema(description = "URL to the user's avatar/profile picture")
    String avatarUrl,

    @Schema(description = "UTC timestamp when the account was created")
    Instant createdAt

) {}
