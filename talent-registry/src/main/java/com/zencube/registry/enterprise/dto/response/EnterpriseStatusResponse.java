package com.zencube.registry.enterprise.dto.response;

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
@Schema(description = "Response object containing detailed enterprise registration status")
public class EnterpriseStatusResponse {

    @Schema(description = "Unique ID of the enterprise account", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID enterpriseId;

    @Schema(description = "Registered company name", example = "ZenCube Tech")
    private String companyName;

    @Schema(description = "Overall status code", example = "PENDING")
    private String status;

    @Schema(description = "User friendly status message", example = "Your application is under review (1-3 business days)")
    private String statusMessage;

    @Schema(description = "Whether the account is actively allowed to use the platform", example = "false")
    private boolean accountActive;

    @Schema(description = "Timestamp when the enterprise was created", example = "2026-06-17T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the enterprise was approved", example = "2026-06-17T12:00:00Z")
    private Instant approvedAt;

    @Schema(description = "Timestamp when the enterprise was rejected", example = "2026-06-17T12:00:00Z")
    private Instant rejectedAt;

    @Schema(description = "Reason for rejection if rejected", example = "Invalid registration number")
    private String rejectionReason;

    @Schema(description = "Timestamp when the enterprise was suspended", example = "2026-06-17T12:00:00Z")
    private Instant suspendedAt;

    @Schema(description = "Reason for suspension if suspended", example = "Violation of terms")
    private String suspensionReason;
}
