package com.zencube.registry.user.dto;

import com.zencube.registry.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request payload for updating a user account.
 *
 * <h2>What Can Be Updated</h2>
 * <p>Only fields that an admin is permitted to modify are included:
 * <ul>
 *   <li>{@code firstName}, {@code lastName} — display name corrections</li>
 *   <li>{@code phone} — contact details</li>
 *   <li>{@code status} — account lifecycle management (ACTIVE, SUSPENDED, etc.)</li>
 *   <li>{@code avatarUrl} — profile photo</li>
 * </ul>
 *
 * <h2>What Cannot Be Updated Here</h2>
 * <ul>
 *   <li>{@code email} — changing email requires a verification flow (separate endpoint)</li>
 *   <li>{@code password} — handled by the reset-password flow</li>
 *   <li>{@code authProvider} — immutable after account creation</li>
 * </ul>
 *
 * <h2>Partial Update Semantics</h2>
 * <p>All fields are optional. The service applies only the non-null fields,
 * leaving unspecified fields unchanged. This avoids the need for a separate PATCH endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating an existing user account")
public class UpdateUserRequest {

    @Schema(description = "Updated first name", example = "Jane")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Schema(description = "Updated last name", example = "Doe")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Schema(description = "Updated phone number", example = "+91 9876543210")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Schema(description = "Account lifecycle status — set to SUSPENDED to temporarily disable",
            example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "URL of the user's profile picture",
            example = "https://cdn.example.com/avatars/jane.jpg")
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
}
