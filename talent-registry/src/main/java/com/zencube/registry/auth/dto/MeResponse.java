package com.zencube.registry.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Response containing the authenticated user's profile and permissions")
public record MeResponse(
        @Schema(description = "The user's unique identifier", example = "uuid")
        UUID id,
        
        @Schema(description = "The user's email address", example = "user@example.com")
        String email,
        
        @Schema(description = "Authentication provider used to create the account", example = "OAUTH2")
        String authType,
        
        @Schema(description = "Whether the user's email has been verified")
        boolean emailVerified,
        
        @Schema(description = "User's timezone setting", example = "UTC")
        String timezone,
        
        @Schema(description = "Roles assigned to the user", example = "[\"STUDENT\"]")
        List<String> roles,
        
        @Schema(description = "Effective permissions aggregated from all roles", example = "[\"APPLICATION_CREATE\", \"PROFILE_VIEW_OWN\"]")
        List<String> permissions,
        
        @Schema(description = "Timestamp of last successful login", example = "2024-01-15T10:30:00Z")
        Instant lastLoginAt,
        
        @Schema(description = "Timestamp of account creation", example = "2024-01-01T00:00:00Z")
        Instant createdAt
) {
}
