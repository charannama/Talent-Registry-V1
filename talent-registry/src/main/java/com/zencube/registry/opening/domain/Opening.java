package com.zencube.registry.opening.domain;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.enums.WorkMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_openings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opening extends BaseEntity {

    @NotNull(message = "Enterprise is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private EnterpriseAccount enterprise;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 50)
    private JobType jobType;

    @Column(name = "domain", length = 100)
    private String domain;

    @Column(name = "salary_min")
    private BigDecimal salaryMin;

    @Column(name = "salary_max")
    private BigDecimal salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 50)
    private WorkMode workMode;

    @Column(name = "positions")
    private Integer positions;

    @Column(name = "application_deadline")
    private Instant applicationDeadline;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OpeningStatus status;

    @Column(name = "required_skills", length = 1000)
    private String requiredSkills;

    @Column(name = "graduation_years", length = 255)
    private String graduationYears;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "revision_requested_by")
    private UUID revisionRequestedBy;

    @Column(name = "revision_requested_at")
    private Instant revisionRequestedAt;

    @Column(name = "revision_feedback", columnDefinition = "TEXT")
    private String revisionFeedback;

    @Column(name = "revision_count", nullable = false)
    @Builder.Default
    private Integer revisionCount = 0;

    @Column(name = "last_resubmitted_at")
    private Instant lastResubmittedAt;

    @Column(name = "last_resubmitted_by")
    private UUID lastResubmittedBy;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closure_reason", columnDefinition = "TEXT")
    private String closureReason;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "can_resubmit")
    private Boolean canResubmit;

    @Column(name = "graduation_year_filter")
    private Integer graduationYearFilter;

    @Column(name = "salary_range_min")
    private BigDecimal salaryRangeMin;

    @Column(name = "salary_range_max")
    private BigDecimal salaryRangeMax;

    public void markFeatured() {
        this.featured = true;
    }

    public void removeFeatured() {
        this.featured = false;
    }

    public boolean supportsGraduationYear(Integer year) {
        if (this.graduationYearFilter == null) return true;
        return this.graduationYearFilter.equals(year);
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = OpeningStatus.DRAFT;
        }
    }

    public boolean canTransitionTo(OpeningStatus newStatus) {
        if (this.status == newStatus) return false;
        
        return switch (this.status) {
            case DRAFT -> newStatus == OpeningStatus.PENDING_APPROVAL;
            case PENDING_APPROVAL -> newStatus == OpeningStatus.LIVE || newStatus == OpeningStatus.REJECTED || newStatus == OpeningStatus.REVISION_REQUESTED;
            case REVISION_REQUESTED -> newStatus == OpeningStatus.PENDING_APPROVAL;
            case LIVE -> newStatus == OpeningStatus.CLOSED;
            case CLOSED -> newStatus == OpeningStatus.ARCHIVED;
            case REJECTED, ARCHIVED -> false;
        };
    }

    public void submit() {
        if (this.status != OpeningStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT openings can be submitted for approval.");
        }
        if (!canTransitionTo(OpeningStatus.PENDING_APPROVAL)) {
            throw new IllegalStateException("Invalid state transition to PENDING_APPROVAL.");
        }
        this.status = OpeningStatus.PENDING_APPROVAL;
    }

    public void approve(UUID hrUserId) {
        if (this.status != OpeningStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending openings can be approved.");
        }
        this.status = OpeningStatus.LIVE;
        this.approvedBy = hrUserId;
        this.approvedAt = Instant.now();
        this.publishedAt = Instant.now();
    }

    public void reject(UUID hrUserId, String reason, Boolean canResubmit) {
        if (this.status != OpeningStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending openings can be rejected.");
        }
        this.status = OpeningStatus.REJECTED;
        this.rejectedBy = hrUserId;
        this.rejectedAt = Instant.now();
        this.rejectionReason = reason;
        this.canResubmit = canResubmit;
    }

    public void close(UUID actorId, String reason) {
        if (this.status != OpeningStatus.LIVE) {
            throw new IllegalStateException("Only live openings can be closed.");
        }
        if (reason != null && reason.length() > 5000) {
            throw new IllegalStateException("Closure reason must not exceed 5000 characters.");
        }
        this.status = OpeningStatus.CLOSED;
        this.closedBy = actorId;
        this.closedAt = Instant.now();
        this.closureReason = reason;
    }

    public boolean isClosed() {
        return this.status == OpeningStatus.CLOSED;
    }

    public void archive() {
        if (this.status != OpeningStatus.CLOSED) {
            throw new IllegalStateException("Only closed openings can be archived.");
        }
        this.status = OpeningStatus.ARCHIVED;
    }

    public void requestRevision(UUID hrUserId, String feedback) {
        if (this.status != OpeningStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending openings can have revisions requested.");
        }
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalStateException("Revision feedback is mandatory.");
        }
        if (feedback.length() > 5000) {
            throw new IllegalStateException("Revision feedback must not exceed 5000 characters.");
        }
        this.status = OpeningStatus.REVISION_REQUESTED;
        this.revisionRequestedBy = hrUserId;
        this.revisionRequestedAt = Instant.now();
        this.revisionFeedback = feedback;
        this.revisionCount = (this.revisionCount == null ? 0 : this.revisionCount) + 1;
    }

    public void resubmit(UUID enterpriseUserId) {
        if (this.status != OpeningStatus.REVISION_REQUESTED) {
            throw new IllegalStateException("Only openings with requested revisions can be resubmitted.");
        }
        this.status = OpeningStatus.PENDING_APPROVAL;
        this.lastResubmittedBy = enterpriseUserId;
        this.lastResubmittedAt = Instant.now();
    }

    public boolean canBeEdited() {
        return this.status == OpeningStatus.DRAFT || this.status == OpeningStatus.REVISION_REQUESTED;
    }

    public boolean isRevisionRequested() {
        return this.status == OpeningStatus.REVISION_REQUESTED;
    }

    public boolean isVisible() {
        return this.status == OpeningStatus.LIVE;
    }

    public boolean canReceiveApplications() {
        return this.status == OpeningStatus.LIVE && 
               this.applicationDeadline != null && 
               this.applicationDeadline.isAfter(Instant.now());
    }
}
