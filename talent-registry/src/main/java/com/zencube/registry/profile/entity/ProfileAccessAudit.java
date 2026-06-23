package com.zencube.registry.profile.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.enums.AccessReason;
import com.zencube.registry.profile.enums.AccessResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_access_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileAccessAudit extends BaseEntity {

    @Column(name = "viewer_user_id", nullable = false)
    private UUID viewerUserId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessReason accessReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessResult accessResult;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private Instant accessedAt;
}
