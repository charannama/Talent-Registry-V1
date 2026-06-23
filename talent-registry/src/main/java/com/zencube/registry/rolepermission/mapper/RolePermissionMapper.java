package com.zencube.registry.rolepermission.mapper;

import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.rolepermission.dto.RolePermissionResponse;
import com.zencube.registry.rolepermission.entity.RolePermission;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manual mapper between {@link RolePermission} entities and their DTO projections.
 *
 * <h2>Responsibility</h2>
 * <p>This class has one responsibility: converting a {@link RolePermission} JPA entity
 * (with its lazily-loaded {@link Role} and {@link Permission} associations) into a
 * flat, serialisation-safe {@link RolePermissionResponse} DTO.
 *
 * <h2>Why Manual Mapping?</h2>
 * <p>Consistent with the {@code PermissionMapper} and {@code AuthMapper} already in the
 * project — no MapStruct dependency is added. Manual mapping keeps the conversion logic
 * transparent, testable without annotation processors, and framework-agnostic.
 *
 * <h2>Lazy Loading Note</h2>
 * <p>Accessing {@code rolePermission.getRole()} and {@code rolePermission.getPermission()}
 * inside an active Hibernate session (i.e. within a {@code @Transactional} boundary)
 * is safe and will trigger a single proxy-initialisation SELECT per association.
 * The {@link com.zencube.registry.rolepermission.repository.RolePermissionRepository#findAllActiveWithDetails()}
 * method uses a JPQL {@code JOIN FETCH} to pre-load both associations for the getAll
 * use-case, eliminating N+1 queries entirely.
 */
@Component
public class RolePermissionMapper {

    // ------------------------------------------------------------------
    // Entity → Response
    // ------------------------------------------------------------------

    /**
     * Maps a single {@link RolePermission} entity to a {@link RolePermissionResponse} DTO.
     *
     * @param rp the entity to map; must not be {@code null}
     * @return a read-only DTO projection
     */
    public RolePermissionResponse toResponse(RolePermission rp) {
        if (rp == null) {
            return null;
        }
        Role role             = rp.getRole();
        Permission permission = rp.getPermission();

        return RolePermissionResponse.builder()
                .id(rp.getId())
                .roleId(role.getId())
                .roleName(role.getName())
                .permissionId(permission.getId())
                .permissionName(permission.getName())
                .permissionCode(permission.getCode())
                .createdAt(rp.getCreatedAt())
                .build();
    }

    // ------------------------------------------------------------------
    // List<Entity> → List<Response>
    // ------------------------------------------------------------------

    /**
     * Maps a list of {@link RolePermission} entities to a list of {@link RolePermissionResponse} DTOs.
     *
     * @param mappings the entities to map; must not be {@code null}
     * @return a list of DTOs in the same order as the input
     */
    public List<RolePermissionResponse> toResponseList(List<RolePermission> mappings) {
        return mappings.stream()
                .map(this::toResponse)
                .toList();
    }
}
