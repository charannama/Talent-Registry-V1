package com.zencube.registry.enterprise.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Enterprise Dashboard Metrics Response")
public class EnterpriseDashboardMetricsResponse {

    @Schema(description = "Total number of enterprises registered")
    private long totalEnterprises;

    @Schema(description = "Total number of pending enterprises")
    private long pendingEnterprises;

    @Schema(description = "Total number of approved enterprises")
    private long approvedEnterprises;

    @Schema(description = "Total number of rejected enterprises")
    private long rejectedEnterprises;

    @Schema(description = "Total number of suspended enterprises")
    private long suspendedEnterprises;

    @Schema(description = "Percentage of approved enterprises compared to total processed")
    private double approvalRate;

    @Schema(description = "Percentage of rejected enterprises compared to total processed")
    private double rejectionRate;
    
    @Schema(description = "Percentage of suspended enterprises compared to total approved")
    private double suspensionRate;

    @Schema(description = "Average time taken to approve an enterprise in days")
    private double averageApprovalTimeDays;

    @Schema(description = "Average time taken to approve an enterprise in hours")
    private double averageApprovalTimeHours;

    @Schema(description = "The date range applied for this report (e.g. ALL_TIME, LAST_30_DAYS)")
    private String dateRange;

    @Schema(description = "The start timestamp of the applied filter")
    private Instant rangeStart;

    @Schema(description = "The end timestamp of the applied filter")
    private Instant rangeEnd;

    @Schema(description = "Timestamp when this report was generated")
    private Instant generatedAt;
}
