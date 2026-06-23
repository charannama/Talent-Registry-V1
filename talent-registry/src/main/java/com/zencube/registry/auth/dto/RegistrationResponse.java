package com.zencube.registry.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Outbound payload for successful registration.
 */
@Schema(description = "Response returned after successful registration")
public record RegistrationResponse(
    @Schema(description = "Whether the registration succeeded", example = "true")
    boolean success,

    @Schema(description = "Status message", example = "Registration successful. Please verify your email.")
    String message
) {}
