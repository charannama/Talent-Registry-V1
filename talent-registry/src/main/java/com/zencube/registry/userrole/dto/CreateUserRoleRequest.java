package com.zencube.registry.userrole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 1. Purpose
 * Request payload for assigning a role to a user.
 *
 * 2. Layer
 * DTO Layer / API Input.
 *
 * 4. Annotation Explanation
 * @Data, @NoArgsConstructor, @AllArgsConstructor: Lombok annotations for boilerplate code.
 * @Schema: Swagger documentation for the API models.
 * @NotNull: Enforces that both UUIDs are provided.
 *
 * 5. Business Logic Explanation
 * This DTO simply transfers the two necessary identifiers to create a mapping.
 * There is no UpdateUserRoleRequest because assignments are immutable; you either assign or revoke.
 *
 * 6. Best Practices
 * - Separate input/output DTOs to prevent accidental exposure of internal fields or mass-assignment vulnerabilities.
 * - Validation annotations are placed directly on the DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for assigning a role to a user")
public class CreateUserRoleRequest {

    @Schema(description = "The UUID of the User", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "User ID is required")
    private UUID userId;

    @Schema(description = "The UUID of the Role", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role ID is required")
    private UUID roleId;
}
