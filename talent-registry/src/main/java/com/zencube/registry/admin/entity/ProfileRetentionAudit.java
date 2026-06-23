package com.zencube.registry.admin.entity;

import com.zencube.registry.admin.enums.FreezeReason;
import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.profile.entity.StudentProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "profile_retention_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRetentionAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private StudentProfile profile;

    @Column(nullable = false)
    private Instant checkedAt;

    private String checkedBy;

    @Column(nullable = false)
    private Boolean canDelete;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FreezeReason freezeReason;

    private Instant retentionExpiresAt;

    private Integer activeApplicationCount;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus mostAdvancedStatus;
}
