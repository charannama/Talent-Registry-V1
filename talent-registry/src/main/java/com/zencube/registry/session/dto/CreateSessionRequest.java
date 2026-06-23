package com.zencube.registry.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 1. Purpose
 * Request payload for manually registering a session (or testing session creation).
 * In a real flow, sessions are often created internally by the Auth module during login.
 *
 * 2. Layer
 * DTO / API Input.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new session mapping")
public class CreateSessionRequest {

    @Schema(description = "User ID assigning the session")
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Schema(description = "The raw refresh token to hash and store")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    @Schema(description = "IP address of the client")
    private String ipAddress;

    @Schema(description = "User-Agent string of the client's browser/device")
    private String userAgent;
}
