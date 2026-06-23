package com.zencube.registry.rolepermission.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only projection of a {@link com.zencube.registry.rolepermission.entity.RolePermission}
 * mapping, enriched with human-readable names from the associated Role and Permission.
 *
 * <h2>Included Fields</h2>
 * <ul>
 *   <li>{@code id}             — UUID of the mapping record itself (for DELETE by ID)</li>
 *   <li>{@code roleId}         — UUID of the role (useful for client-side filtering)</li>
 *   <li>{@code roleName}       — human-readable role label</li>
 *   <li>{@code permissionId}   — UUID of the permission</li>
 *   <li>{@code permissionName} — human-readable permission label</li>
 *   <li>{@code permissionCode} — machine-readable code for authorization checks</li>
 *   <li>{@code createdAt}      — when the mapping was created (audit)</li>
 * </ul>
 *
 * <h2>Excluded Fields</h2>
 * <p>Sensitive audit fields ({@code deletedAt}, {@code deletedBy}, {@code version})
 * are intentionally omitted from API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionResponse {

    /** UUID of the role-permission mapping record. */
    private UUID id;

    /** UUID of the associated role. */
    private UUID roleId;

    /** Human-readable name of the role (e.g. "Admin"). */
    private String roleName;

    /** UUID of the associated permission. */
    private UUID permissionId;

    /** Human-readable label of the permission (e.g. "View Students"). */
    private String permissionName;

    /**
     * Machine-readable permission code (e.g. "VIEW_STUDENTS").
     * Matches the value used in {@code @PreAuthorize} expressions.
     */
    private String permissionCode;

    /** Timestamp at which this mapping was created. */
    private Instant createdAt;
}
