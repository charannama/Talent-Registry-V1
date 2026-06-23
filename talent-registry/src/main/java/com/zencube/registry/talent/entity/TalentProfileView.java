package com.zencube.registry.talent.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.entity.StudentProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "talent_profile_views",
    indexes = {
        @Index(name = "idx_talent_profile_views_profile", columnList = "profile_id"),
        @Index(name = "idx_talent_profile_views_enterprise", columnList = "enterprise_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentProfileView extends BaseEntity {

    @Column(name = "enterprise_id", nullable = false)
    private UUID enterpriseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private StudentProfile profile;

    @Column(nullable = false)
    @Builder.Default
    private Instant viewedAt = Instant.now();

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;
}
