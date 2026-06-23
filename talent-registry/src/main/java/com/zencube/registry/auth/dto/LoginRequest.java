package com.zencube.registry.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Inbound payload for user login.
 */
@Schema(description = "Credentials for authenticating an existing user")
public record LoginRequest(

    @Schema(description = "Registered email address", example = "jane.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    String email,

    @Schema(description = "Account password", example = "Secure@123")
    @NotBlank(message = "Password is required")
    String password

) {}
