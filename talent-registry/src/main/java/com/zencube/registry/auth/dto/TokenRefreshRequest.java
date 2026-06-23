package com.zencube.registry.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Inbound payload for exchanging a refresh token for a new access token.
 */
@Schema(description = "Request body for refreshing an access token")
public record TokenRefreshRequest(

    @Schema(description = "The raw refresh token issued at login",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "Refresh token is required")
    String refreshToken

) {}
