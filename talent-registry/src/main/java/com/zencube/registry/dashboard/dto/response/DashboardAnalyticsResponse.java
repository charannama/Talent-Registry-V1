package com.zencube.registry.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsResponse {
    private PipelineMetricsResponse pipelineMetrics;
    private EnterpriseMetricsResponse enterpriseMetrics;
    private OpeningMetricsResponse openingMetrics;
    private ConversionMetricsResponse conversionMetrics;
    private Instant generatedAt;
}
