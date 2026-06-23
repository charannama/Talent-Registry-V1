package com.zencube.registry.userrole.mapper;

import com.zencube.registry.userrole.dto.UserRoleResponse;
import com.zencube.registry.userrole.entity.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 1. Purpose
 * Maps UserRole entities to UserRoleResponse DTOs.
 *
 * 2. Layer
 * Mapper Component.
 *
 * 4. Annotation Explanation
 * @Component: Registers this class as a Spring Bean so it can be injected into the Service.
 *
 * 5. Business Logic Explanation
 * Extracts scalar values from the entity graph (User and Role relationships) to create
 * a flat response object. Handles potential nulls gracefully.
 *
 * 6. Best Practices
 * - Written manually to avoid MapStruct annotation processing complexity during early development.
 * - Extracts `email` from User and `name` from Role so the API consumer gets human-readable context.
 */
@Component
public class UserRoleMapper {

    public UserRoleResponse toResponse(UserRole entity) {
        if (entity == null) {
            return null;
        }

        return UserRoleResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .email(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .roleId(entity.getRole() != null ? entity.getRole().getId() : null)
                .roleName(entity.getRole() != null ? entity.getRole().getName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<UserRoleResponse> toResponseList(List<UserRole> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
