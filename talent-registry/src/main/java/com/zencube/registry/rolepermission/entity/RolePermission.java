package com.zencube.registry.rolepermission.entity;

import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Join entity that represents the many-to-many relationship between
 * {@link Role} and {@link Permission} in the RBAC system.
 *
 * <h2>Why a dedicated entity instead of {@code @ManyToMany}?</h2>
 * <p>A plain {@code @ManyToMany} annotation generates a hidden join table that:
 * <ul>
 *   <li>Cannot be queried independently (no UUID, no audit fields)</li>
 *   <li>Cannot be soft-deleted individually</li>
 *   <li>Cannot carry additional metadata in future (e.g. granted_by, expires_at)</li>
 * </ul>
 * By promoting the join table to a first-class {@code @Entity}, we gain full
 * auditability, REST addressability (each mapping has its own {@code id}),
 * and a clean extension point for future fields.
 *
 * <h2>Relationship Mapping</h2>
 * <ul>
 *   <li>{@code role}       → {@code @ManyToOne LAZY} — many mappings share one role</li>
 *   <li>{@code permission} → {@code @ManyToOne LAZY} — many mappings share one permission</li>
 * </ul>
 *
 * <h2>Unique Constraint</h2>
 * <p>The composite unique constraint {@code uq_role_permission} prevents
 * the same (role, permission) pair from being inserted more than once.
 * The service-layer guard ({@code existsByRoleAndPermission}) enforces this
 * before hitting the database to produce a friendly 409 response.
 */
@Entity
@Table(
    name = "role_permissions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_role_permission",
            columnNames = {"role_id", "permission_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission extends BaseEntity {

    /**
     * The role to which this permission is assigned.
     *
     * <p>Loaded LAZILY to avoid fetching the full {@link Role} graph
     * every time a {@link RolePermission} record is read.
     * The {@link com.zencube.registry.rolepermission.mapper.RolePermissionMapper}
     * accesses only {@code role.getName()} during response mapping, which
     * triggers a single secondary SELECT — acceptable and predictable.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * The permission being assigned to the role.
     *
     * <p>Loaded LAZILY for the same performance reason as {@link #role}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
}
