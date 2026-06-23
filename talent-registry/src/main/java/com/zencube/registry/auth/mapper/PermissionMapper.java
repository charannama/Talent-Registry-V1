package com.zencube.registry.auth.mapper;

import com.zencube.registry.auth.dto.permission.CreatePermissionRequest;
import com.zencube.registry.auth.dto.permission.PermissionResponse;
import com.zencube.registry.auth.entity.Permission;
import org.springframework.stereotype.Component;

/**
 * Manual mapper between {@link Permission} entities and permission-layer DTOs.
 *
 * <p>Deliberately hand-written (no MapStruct) to stay consistent with the
 * existing {@link AuthMapper} pattern in this package and to keep the mapping
 * logic explicit and easy to maintain.
 */
@Component
public class PermissionMapper {

    // ------------------------------------------------------------------
    // CreatePermissionRequest  →  Permission (new entity, not yet persisted)
    // ------------------------------------------------------------------

    /**
     * Builds a new, un-persisted {@link Permission} entity from a create request.
     *
     * @param request the inbound creation payload
     * @return a transient {@link Permission} ready to be saved
     */
    public Permission toEntity(CreatePermissionRequest request) {
        return Permission.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();
    }

    // ------------------------------------------------------------------
    // Permission  →  PermissionResponse
    // ------------------------------------------------------------------

    /**
     * Maps a persisted {@link Permission} entity to a {@link PermissionResponse} DTO.
     *
     * @param permission the entity to map; must not be {@code null}
     * @return a read-only DTO projection of the permission
     */
    public PermissionResponse toResponse(Permission permission) {
        if (permission == null) {
            return null;
        }
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .code(permission.getCode())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
