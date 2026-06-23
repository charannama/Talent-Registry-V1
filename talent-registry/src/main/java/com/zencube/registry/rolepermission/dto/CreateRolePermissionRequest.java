package com.zencube.registry.rolepermission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request payload for assigning a {@link com.zencube.registry.auth.entity.Permission}
 * to a {@link com.zencube.registry.auth.entity.Role}.
 *
 * <h2>Why no UpdateRequest?</h2>
 * <p>A role-permission mapping is an immutable relationship record.
 * There is nothing to "update" — the only operations that make sense
 * are <em>assign</em> (POST) and <em>revoke</em> (DELETE).
 * Modifying a mapping would effectively be a delete-and-recreate,
 * so no {@code UpdateRolePermissionRequest} is provided.
 *
 * <p>Both {@code roleId} and {@code permissionId} are required.
 * The service resolves each UUID to its respective entity and validates
 * existence before creating the mapping.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRolePermissionRequest {

    /**
     * UUID of the {@link com.zencube.registry.auth.entity.Role} to which
     * the permission will be assigned.
     */
    @NotNull(message = "Role ID is required")
    private UUID roleId;

    /**
     * UUID of the {@link com.zencube.registry.auth.entity.Permission}
     * to assign to the role.
     */
    @NotNull(message = "Permission ID is required")
    private UUID permissionId;
}
