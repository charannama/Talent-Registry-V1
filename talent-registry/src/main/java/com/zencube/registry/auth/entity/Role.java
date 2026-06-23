package com.zencube.registry.auth.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.common.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @OneToMany(mappedBy = "role")
    private java.util.Set<com.zencube.registry.userrole.entity.UserRole> userRoles;

    @OneToMany(mappedBy = "role")
    private java.util.Set<com.zencube.registry.rolepermission.entity.RolePermission> rolePermissions;
}
