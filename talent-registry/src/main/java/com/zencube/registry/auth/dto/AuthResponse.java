package com.zencube.registry.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Outbound response returned after a successful login or token refresh.
 * Contains both tokens and basic user information so the client does
 * not need to make a separate /me call immediately after login.
 */
@Schema(description = "Authentication response containing JWT tokens and basic user info")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(

    @Schema(description = "Short-lived JWT access token")
    String accessToken,

    @Schema(description = "Long-lived refresh token for obtaining new access tokens")
    String refreshToken,

    @Schema(description = "Token type – always 'Bearer'", example = "Bearer")
    String tokenType,

    @Schema(description = "Access token expiry in seconds", example = "900")
    long expiresIn,

    @Schema(description = "Authenticated user's UUID")
    UUID userId,

    @Schema(description = "Authenticated user's email", example = "jane.doe@example.com")
    String email,

    @Schema(description = "Authenticated user's display name", example = "Jane Doe")
    String displayName

) {

    /** Convenience factory that sets {@code tokenType} to {@code "Bearer"}. */
    public static AuthResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UUID userId,
            String email,
            String displayName) {

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                userId,
                email,
                displayName);
    }
}
