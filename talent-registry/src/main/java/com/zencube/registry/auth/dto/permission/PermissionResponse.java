package com.zencube.registry.auth.dto.permission;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only projection of a {@link com.zencube.registry.auth.entity.Permission} entity.
 *
 * <p>Sensitive audit fields (deletedAt, deletedBy, version) are intentionally omitted
 * to reduce payload size and avoid leaking internal implementation details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {

    /** Unique identifier of the permission. */
    private UUID id;

    /** Human-readable label, e.g. "View Students". */
    private String name;

    /**
     * Machine-readable code used in authorization expressions,
     * e.g. "VIEW_STUDENTS".
     */
    private String code;

    /** Optional description of what this permission grants. */
    private String description;

    /** Timestamp at which the permission was first created. */
    private Instant createdAt;

    /** Timestamp of the most recent update. */
    private Instant updatedAt;
}
