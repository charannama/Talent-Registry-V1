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
@Schema(description = "Response object containing enterprise approval details")
public class EnterpriseApprovalResponse {

    @Schema(description = "Unique ID of the enterprise account", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID enterpriseId;

    @Schema(description = "Registered company name", example = "ZenCube Tech")
    private String companyName;

    @Schema(description = "Current status of the enterprise", example = "APPROVED")
    private String status;

    @Schema(description = "Whether the account is fully active", example = "true")
    private boolean accountActive;

    @Schema(description = "Timestamp when the enterprise was approved", example = "2026-06-17T12:00:00Z")
    private Instant approvedAt;

    @Schema(description = "ID of the HR user who approved the application", example = "11223344-5566-7788-99aa-bbccddeeff00")
    private UUID approvedBy;

    @Schema(description = "Success message", example = "Enterprise successfully approved")
    private String message;
}
