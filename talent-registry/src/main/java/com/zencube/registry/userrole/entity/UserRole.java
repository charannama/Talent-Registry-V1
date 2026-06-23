package com.zencube.registry.userrole.entity;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 1. Purpose
 * Represents the assignment of a Role to a User.
 * Acts as the bridge table between `users` and `roles` allowing fine-grained RBAC.
 *
 * 2. Layer
 * Entity / Persistence Layer. Maps to the `user_roles` database table.
 *
 * 4. Annotation Explanation
 * @Entity & @Table: Marks this as a JPA managed entity mapped to the "user_roles" table.
 * @ManyToOne: Defines the relationships back to User and Role, loaded lazily.
 *
 * 5. Business Logic Explanation
 * By extending BaseEntity, this assignment inherits soft-delete and audit capabilities.
 * The `is_deleted` flag is critical because we don't hard-delete mappings; we revoke them
 * while keeping an audit trail.
 *
 * 6. Best Practices
 * - FetchType.LAZY on @ManyToOne prevents N+1 eager loading issues when querying User/Role.
 * - Extending BaseEntity provides a unified audit trail across the platform.
 *
 * 7. Common Mistakes
 * - Omitting FetchType.LAZY leads to severe performance degradation.
 * - Using a simple @ManyToMany on User/Role instead of a promoted entity hides the relationship
 *   from explicit REST control and auditing.
 */
@Entity
@Table(
    name = "user_roles",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_role", columnNames = {"user_id", "role_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
