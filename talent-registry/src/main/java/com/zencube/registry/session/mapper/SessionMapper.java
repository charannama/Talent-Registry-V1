package com.zencube.registry.session.mapper;

import com.zencube.registry.session.dto.SessionResponse;
import com.zencube.registry.session.entity.Session;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 1. Purpose
 * Maps internal Session entities to external SessionResponse DTOs.
 *
 * 2. Layer
 * Mapper Component.
 */
@Component
public class SessionMapper {

    public SessionResponse toResponse(Session entity) {
        if (entity == null) {
            return null;
        }

        return SessionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .expiresAt(entity.getExpiresAt())
                .revokedAt(entity.getRevokedAt())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .createdAt(entity.getCreatedAt())
                .isActive(entity.isValid())
                .build();
    }

    public List<SessionResponse> toResponseList(List<Session> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
