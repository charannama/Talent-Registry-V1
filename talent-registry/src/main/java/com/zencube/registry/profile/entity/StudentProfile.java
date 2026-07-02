package com.zencube.registry.profile.entity;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.enums.EligibilityLevel;
import com.zencube.registry.profile.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true
    )
    private User user;

    private String avatarUrl;

    private String institution;

    private String discipline;

    private Integer graduationYear;
    
    @Column(name = "graduation_date")
    private java.time.LocalDate graduationDate;

    private Double gpa;

    private String coursework;

    private String location;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private Boolean fullTimeReady;

    private Boolean internshipReady;

    private Boolean remotePreference;

    @Enumerated(EnumType.STRING)
    private EligibilityLevel eligibilityLevel;

    private Instant lastSyncAt;

    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus;

    @Column(columnDefinition = "TEXT")
    private String syncError;

    // --- Talent Search Fields ---
    @Builder.Default
    private Boolean profileVisible = true;

    @Builder.Default
    private Boolean suspended = false;

    @Builder.Default
    private Long profileViews = 0L;

    @Enumerated(EnumType.STRING)
    private com.zencube.registry.profile.enums.ProjectType highestProjectType;

    @Builder.Default
    private Boolean searchable = true;

    @Builder.Default
    private Boolean talentQualified = false;

    @Column(columnDefinition = "TEXT")
    private String suspensionReason;

    private Instant suspendedAt;

    private UUID suspendedBy;

    public void suspend(UUID adminId, String reason) {
        this.suspended = true;
        this.profileVisible = false;
        this.suspendedAt = Instant.now();
        this.suspendedBy = adminId;
        this.suspensionReason = reason;
    }

    public void reactivate() {
        this.suspended = false;
        this.profileVisible = true;
        this.suspendedAt = null;
        this.suspendedBy = null;
        this.suspensionReason = null;
    }

    public void hideProfile() {
        this.profileVisible = false;
    }

    public void showProfile() {
        if (this.suspended != null && this.suspended) {
            throw new IllegalStateException("Cannot show a suspended profile.");
        }
        this.profileVisible = true;
    }
}
