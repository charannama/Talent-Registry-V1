package com.zencube.registry.profile.dto;

import com.zencube.registry.profile.enums.AccessReason;
import com.zencube.registry.profile.enums.AccessResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileAccessAuditDTO {
    private UUID id;
    private UUID viewerUserId;
    private UUID targetUserId;
    private AccessReason accessReason;
    private AccessResult accessResult;
    private String ipAddress;
    private String userAgent;
    private Instant accessedAt;
    private Instant createdAt;
}
