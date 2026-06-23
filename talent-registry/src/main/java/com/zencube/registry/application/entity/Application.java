package com.zencube.registry.application.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.profile.entity.StudentProfile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications", uniqueConstraints = {
    @UniqueConstraint(name = "uk_application_profile_opening", columnNames = {"profile_id", "opening_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private StudentProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opening_id", nullable = false)
    private com.zencube.registry.opening.domain.Opening opening;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(name = "applied_at")
    private java.time.Instant appliedAt;

    @Column(name = "forwarded_at")
    private java.time.Instant forwardedAt;

    @Column(name = "last_stage_updated_at")
    private java.time.Instant lastStageUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_attachment_id")
    private com.zencube.registry.attachment.entity.Attachment resume;
}
