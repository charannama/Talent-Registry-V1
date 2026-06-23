package com.zencube.registry.profile.mapper;

import com.zencube.registry.profile.dto.ProfileAccessAuditDTO;
import com.zencube.registry.profile.entity.ProfileAccessAudit;
import org.springframework.stereotype.Component;

@Component
public class ProfileAccessAuditMapper {

    public ProfileAccessAuditDTO toDto(ProfileAccessAudit entity) {
        if (entity == null) {
            return null;
        }

        return ProfileAccessAuditDTO.builder()
                .id(entity.getId())
                .viewerUserId(entity.getViewerUserId())
                .targetUserId(entity.getTargetUserId())
                .accessReason(entity.getAccessReason())
                .accessResult(entity.getAccessResult())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .accessedAt(entity.getAccessedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
