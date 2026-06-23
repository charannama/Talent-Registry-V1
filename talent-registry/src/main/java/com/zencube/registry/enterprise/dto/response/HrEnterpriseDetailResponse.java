package com.zencube.registry.enterprise.dto.response;

import com.zencube.registry.enterprise.enums.CompanySize;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Detailed Response DTO for HR Enterprise View")
public class HrEnterpriseDetailResponse {

    @Schema(description = "Unique ID of the enterprise account")
    private UUID id;

    @Schema(description = "ID of the User who owns this account")
    private UUID userId;

    @Schema(description = "Registered company name")
    private String companyName;

    @Schema(description = "Business domain email")
    private String domainEmail;

    @Schema(description = "Company website URL")
    private String companyWebsite;

    @Schema(description = "Business registration number")
    private String registrationNumber;

    @Schema(description = "Industry classification")
    private String industry;

    @Schema(description = "Detailed company description")
    private String companyDescription;

    @Schema(description = "Address Line 1")
    private String addressLine1;

    @Schema(description = "Address Line 2")
    private String addressLine2;

    @Schema(description = "City")
    private String city;

    @Schema(description = "State/Province")
    private String state;

    @Schema(description = "Country")
    private String country;

    @Schema(description = "Postal Code")
    private String postalCode;

    @Schema(description = "Size of the company")
    private CompanySize companySize;

    @Schema(description = "URL to the company logo")
    private String logoUrl;

    @Schema(description = "GST / Tax Number")
    private String gstNumber;

    @Schema(description = "Sector classification")
    private String sector;

    @Schema(description = "Name of the hiring manager")
    private String hiringManagerName;

    @Schema(description = "Email of the hiring manager")
    private String hiringManagerEmail;

    @Schema(description = "Phone of the hiring manager")
    private String hiringManagerPhone;

    @Schema(description = "Current HR onboarding status")
    private EnterpriseOnboardingStatus onboardingStatus;

    @Schema(description = "Is the account fully active")
    private Boolean accountActive;

    @Schema(description = "ID of the HR User who approved this")
    private UUID onboardedBy;

    @Schema(description = "Timestamp of approval")
    private Instant approvedAt;

    @Schema(description = "ID of the HR User who rejected this")
    private UUID rejectedBy;

    @Schema(description = "Timestamp of rejection")
    private Instant rejectedAt;

    @Schema(description = "Reason for rejection")
    private String rejectionReason;

    @Schema(description = "ID of the HR User who suspended this")
    private UUID suspendedBy;

    @Schema(description = "Timestamp of suspension")
    private Instant suspendedAt;

    @Schema(description = "Reason for suspension")
    private String suspensionReason;

    @Schema(description = "ID of the HR User who reactivated this")
    private UUID reactivatedBy;

    @Schema(description = "Timestamp of reactivation")
    private Instant reactivatedAt;

    @Schema(description = "Timestamp of last status change")
    private Instant lastStatusChangedAt;

    @Schema(description = "ID of the actor who made the last status change")
    private UUID lastStatusChangedBy;

    @Schema(description = "Record creation timestamp")
    private Instant createdAt;

    @Schema(description = "Record creator UUID string")
    private String createdBy;

    @Schema(description = "Record last update timestamp")
    private Instant updatedAt;

    @Schema(description = "Record last updater UUID string")
    private String updatedBy;
}
