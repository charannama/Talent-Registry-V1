package com.zencube.registry.profile.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "profile_sync_audits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileSyncAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private StudentProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant syncStartTime;
    
    private Instant syncEndTime;
}
