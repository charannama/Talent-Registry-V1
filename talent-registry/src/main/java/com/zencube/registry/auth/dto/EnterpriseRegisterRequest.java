package com.zencube.registry.auth.dto;

import com.zencube.registry.auth.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for enterprise recruiter registration.
 */
@Schema(description = "Request body for creating a new enterprise recruiter account")
public record EnterpriseRegisterRequest(

    @Schema(description = "User's first name", example = "Jane")
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @Schema(description = "Email address used for login", example = "recruiter@company.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @Schema(description = "Password (min 8 chars, upper + lower + digit + special char)", example = "Secure@123")
    @NotBlank(message = "Password is required")
    @ValidPassword
    String password,

    @Schema(description = "Company name", example = "ZenCube")
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    String companyName

) {}
