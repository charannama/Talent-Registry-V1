package com.zencube.registry.user.dto;

import com.zencube.registry.auth.enums.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request payload for admin-created user accounts.
 *
 * <h2>Use Case Distinction</h2>
 * <p>This DTO is consumed by the <em>User Management API</em> ({@code /api/v1/users}),
 * used by admins to provision accounts programmatically. It differs from
 * {@link com.zencube.registry.auth.dto.RegisterRequest} (used by the Auth API
 * for self-service registration) in two ways:
 * <ul>
 *   <li>Admins can specify the {@code authProvider} to create OAuth2-backed accounts.</li>
 *   <li>Admins can omit the password when creating OAuth2 accounts (password is optional).</li>
 * </ul>
 *
 * <h2>Password Handling</h2>
 * <p>Password is optional because an admin may create an OAuth2 user without a local password.
 * If provided, the service layer encodes it with BCrypt before persistence — the raw value
 * is never stored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for admin-provisioned user creation")
public class CreateUserRequest {

    @Schema(description = "First name", example = "Jane", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Schema(description = "Last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Schema(description = "Primary email address — used for login", example = "jane.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    /**
     * Plain-text password — <strong>only for LOCAL accounts</strong>.
     * Nullable for OAuth2 accounts. Min 8, max 128 characters.
     */
    @Schema(description = "Plain-text password (LOCAL accounts only, min 8 characters)",
            example = "Secure@123", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @Schema(description = "Phone number", example = "+91 9876543210",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Schema(description = "Identity provider — defaults to LOCAL",
            example = "LOCAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @NotNull(message = "Auth provider is required")
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;
}
