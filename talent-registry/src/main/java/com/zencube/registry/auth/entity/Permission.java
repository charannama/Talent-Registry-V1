package com.zencube.registry.auth.entity;

import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a fine-grained permission in the RBAC system.
 *
 * <p>Permissions are the atomic units of access control.
 * They are grouped through roles and assigned to users via the RolePermission join.
 *
 * <p>Example permissions: MANAGE_USERS, VIEW_STUDENTS, CREATE_INTERVIEW.
 */
@Entity
@Table(
    name = "permissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_permissions_name", columnNames = "name"),
        @UniqueConstraint(name = "uq_permissions_code", columnNames = "code")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    /** Human-readable label, e.g. "View Students". */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Machine-readable identifier used in {@code @PreAuthorize} expressions,
     * e.g. "VIEW_STUDENTS".  Always uppercase with underscores.
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /** Optional human-readable description of what this permission grants. */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
