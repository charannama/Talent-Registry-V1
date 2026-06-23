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
@Schema(description = "Response object containing enterprise suspension details")
public class EnterpriseSuspensionResponse {

    @Schema(description = "Unique ID of the enterprise account", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID enterpriseId;

    @Schema(description = "Registered company name", example = "ZenCube Tech")
    private String companyName;

    @Schema(description = "Current status of the enterprise", example = "SUSPENDED")
    private String status;

    @Schema(description = "Whether the account is fully active", example = "false")
    private boolean accountActive;

    @Schema(description = "Reason provided for suspension", example = "Violation of Terms of Service")
    private String suspensionReason;

    @Schema(description = "Timestamp when the enterprise was suspended", example = "2026-06-17T12:00:00Z")
    private Instant suspendedAt;

    @Schema(description = "Success message", example = "Enterprise successfully suspended")
    private String message;
}
