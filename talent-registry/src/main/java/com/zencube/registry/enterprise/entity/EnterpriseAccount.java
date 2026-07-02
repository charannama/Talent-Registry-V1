package com.zencube.registry.enterprise.entity;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.enterprise.enums.CompanySize;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enterprise_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = "company_name"),
        @UniqueConstraint(columnNames = "domain_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriseAccount extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @NotBlank(message = "Domain email is required")
    @Email(message = "Must be a valid email address")
    @Column(name = "domain_email", nullable = false, unique = true)
    private String domainEmail;

    @Size(max = 500)
    @Column(name = "company_website", length = 500)
    private String companyWebsite;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_description")
    private String companyDescription;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_size", length = 30)
    private CompanySize companySize;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Size(max = 20)
    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Size(max = 100)
    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "hiring_manager_name")
    private String hiringManagerName;

    @Email(message = "Must be a valid email address")
    @Column(name = "hiring_manager_email")
    private String hiringManagerEmail;

    @Column(name = "hiring_manager_phone")
    private String hiringManagerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", nullable = false, length = 30)
    private EnterpriseOnboardingStatus onboardingStatus;

    @Column(name = "account_active", nullable = false)
    private Boolean accountActive;

    @Column(name = "onboarded_by")
    private UUID onboardedBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "suspended_at")
    private Instant suspendedAt;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "suspended_by")
    private UUID suspendedBy;

    @Column(name = "last_status_changed_at")
    private Instant lastStatusChangedAt;

    @Column(name = "last_status_changed_by")
    private UUID lastStatusChangedBy;

    @Column(name = "reactivated_at")
    private Instant reactivatedAt;

    @Column(name = "reactivated_by")
    private UUID reactivatedBy;

    @PrePersist
    public void prePersist() {
        if (this.onboardingStatus == null) {
            this.onboardingStatus = EnterpriseOnboardingStatus.PENDING_HR_REVIEW;
        }
        if (this.accountActive == null) {
            this.accountActive = false;
        }
    }

    public void approve(UUID hrUserId) {
        if (!this.onboardingStatus.canTransitionTo(EnterpriseOnboardingStatus.APPROVED)) {
            throw new com.zencube.registry.common.exception.BusinessException("Invalid state transition. Cannot transition from " + this.onboardingStatus + " to APPROVED.", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TRANSITION");
        }
        this.onboardingStatus = EnterpriseOnboardingStatus.APPROVED;
        this.accountActive = true;
        this.approvedAt = Instant.now();
        this.approvedBy = hrUserId;
        this.onboardedBy = hrUserId;
        this.lastStatusChangedAt = Instant.now();
        this.lastStatusChangedBy = hrUserId;
        this.rejectedBy = null;
        this.rejectedAt = null;
        this.rejectionReason = null;
        this.suspendedAt = null;
        this.suspensionReason = null;
        this.suspendedBy = null;
    }

    public void reinstate(UUID hrUserId) {
        if (!this.onboardingStatus.canTransitionTo(EnterpriseOnboardingStatus.APPROVED)) {
            throw new com.zencube.registry.common.exception.BusinessException("Invalid state transition. Cannot transition from " + this.onboardingStatus + " to APPROVED.", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TRANSITION");
        }
        this.onboardingStatus = EnterpriseOnboardingStatus.APPROVED;
        this.accountActive = true;
        this.onboardedBy = hrUserId;
        this.lastStatusChangedAt = Instant.now();
        this.lastStatusChangedBy = hrUserId;
        this.reactivatedAt = Instant.now();
        this.reactivatedBy = hrUserId;
        this.suspendedAt = null;
        this.suspensionReason = null;
        this.suspendedBy = null;
    }

    public void reject(String reason, UUID rejectedByActor) {
        if (!this.onboardingStatus.canTransitionTo(EnterpriseOnboardingStatus.REJECTED)) {
            throw new com.zencube.registry.common.exception.BusinessException("Invalid state transition. Cannot transition from " + this.onboardingStatus + " to REJECTED.", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TRANSITION");
        }
        this.onboardingStatus = EnterpriseOnboardingStatus.REJECTED;
        this.accountActive = false;
        this.rejectedAt = Instant.now();
        this.rejectedBy = rejectedByActor;
        this.rejectionReason = reason;
        this.lastStatusChangedAt = Instant.now();
        this.lastStatusChangedBy = rejectedByActor;
        
        // Clear conflicting status fields
        this.approvedAt = null;
        this.onboardedBy = null;
        this.suspendedAt = null;
        this.suspensionReason = null;
        this.suspendedBy = null;
    }

    public void suspend(String reason, UUID suspendedByActor) {
        if (!this.onboardingStatus.canTransitionTo(EnterpriseOnboardingStatus.SUSPENDED)) {
            throw new com.zencube.registry.common.exception.BusinessException("Invalid state transition. Cannot transition from " + this.onboardingStatus + " to SUSPENDED.", org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TRANSITION");
        }
        this.onboardingStatus = EnterpriseOnboardingStatus.SUSPENDED;
        this.accountActive = false;
        this.suspendedAt = Instant.now();
        this.suspensionReason = reason;
        this.suspendedBy = suspendedByActor;
        this.lastStatusChangedAt = Instant.now();
        this.lastStatusChangedBy = suspendedByActor;
    }

    public boolean isOwner(UUID authUserId) {
        return this.user != null && this.user.getId().equals(authUserId);
    }

    public boolean isApproved() {
        return EnterpriseOnboardingStatus.APPROVED.equals(this.onboardingStatus);
    }

    public boolean isPending() {
        return EnterpriseOnboardingStatus.PENDING_HR_REVIEW.equals(this.onboardingStatus);
    }

    public boolean isRejected() {
        return EnterpriseOnboardingStatus.REJECTED.equals(this.onboardingStatus);
    }

    public boolean isSuspended() {
        return EnterpriseOnboardingStatus.SUSPENDED.equals(this.onboardingStatus);
    }

    public boolean canPostOpenings() {
        return isApproved() && Boolean.TRUE.equals(this.accountActive);
    }

    public boolean canSearchTalent() {
        return isApproved() && Boolean.TRUE.equals(this.accountActive);
    }

    public boolean canManageJobs() {
        return isApproved() && Boolean.TRUE.equals(this.accountActive);
    }

    public String getDashboardMessage() {
        if (this.onboardingStatus == null) return "Unknown status.";
        switch (this.onboardingStatus) {
            case APPROVED:
                return "Your account is active.";
            case PENDING_HR_REVIEW:
                return "Your application is under review (1-3 business days).";
            case REJECTED:
                return "Your application was rejected.";
            case SUSPENDED:
                return "Your account is suspended.";
            default:
                return "Unknown status.";
        }
    }
}
