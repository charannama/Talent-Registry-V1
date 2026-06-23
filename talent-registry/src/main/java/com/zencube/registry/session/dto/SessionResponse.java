package com.zencube.registry.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 1. Purpose
 * Read-only representation of a user session.
 *
 * 2. Layer
 * DTO / API Output.
 *
 * 5. Business Logic Explanation
 * Crucially, this output NEVER includes the `refreshTokenHash`. Even the hash is
 * internal security state. We only return identifiers, status, and device info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing session metadata")
public class SessionResponse {

    @Schema(description = "The UUID of the session")
    private UUID id;

    @Schema(description = "The UUID of the user who owns this session")
    private UUID userId;

    @Schema(description = "When the session will expire")
    private Instant expiresAt;

    @Schema(description = "When the session was explicitly revoked, if applicable")
    private Instant revokedAt;

    @Schema(description = "IP address where the session originated")
    private String ipAddress;

    @Schema(description = "Browser or device identifier")
    private String userAgent;

    @Schema(description = "When the session was created")
    private Instant createdAt;

    @Schema(description = "Whether the session is currently active")
    private boolean isActive;
}
