package com.zencube.registry.enterprise.dto.response;

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
@Schema(description = "Response object for Enterprise Dashboard initialization and access control")
public class EnterpriseDashboardResponse {

    @Schema(description = "Unique ID of the enterprise account")
    private UUID enterpriseId;

    @Schema(description = "Registered company name")
    private String companyName;

    @Schema(description = "Current onboarding status")
    private EnterpriseOnboardingStatus status;

    @Schema(description = "User-facing dashboard message")
    private String statusMessage;

    @Schema(description = "Can this enterprise post new job openings?")
    private boolean canPostOpenings;

    @Schema(description = "Can this enterprise search the talent pool?")
    private boolean canSearchTalent;

    @Schema(description = "Can this enterprise manage active job listings?")
    private boolean canManageJobs;

    @Schema(description = "Timestamp when the enterprise was approved")
    private Instant approvedAt;

    @Schema(description = "Timestamp when the enterprise was rejected")
    private Instant rejectedAt;

    @Schema(description = "Reason for rejection, if applicable")
    private String rejectionReason;

    @Schema(description = "Timestamp when the enterprise was suspended")
    private Instant suspendedAt;

    @Schema(description = "Reason for suspension, if applicable")
    private String suspensionReason;

    @Schema(description = "Enterprise creation timestamp")
    private Instant createdAt;

    @Schema(description = "Enterprise last updated timestamp")
    private Instant updatedAt;
}
