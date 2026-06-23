package com.zencube.registry.userrole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 1. Purpose
 * Read-only projection of a UserRole mapping for API consumers.
 *
 * 2. Layer
 * DTO Layer / API Output.
 *
 * 4. Annotation Explanation
 * @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor: Lombok annotations for boilerplates.
 * @Schema: Swagger documentation details.
 *
 * 5. Business Logic Explanation
 * Flattens the nested User and Role entities into a single level response containing
 * the most useful descriptive fields (e.g., email and roleName) alongside the IDs.
 *
 * 6. Best Practices
 * - Never return the JPA Entity directly from the controller.
 * - Provide display-friendly fields (like names) so frontends don't have to make N additional calls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing a user-role assignment")
public class UserRoleResponse {

    @Schema(description = "The UUID of the mapping itself")
    private UUID id;

    @Schema(description = "The UUID of the assigned User")
    private UUID userId;

    @Schema(description = "The email address of the User")
    private String email;

    @Schema(description = "The UUID of the assigned Role")
    private UUID roleId;

    @Schema(description = "The name of the Role")
    private String roleName;

    @Schema(description = "When the role was assigned")
    private Instant createdAt;
}
