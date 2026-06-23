package com.zencube.registry.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseMetricsResponse {
    private Map<String, Map<String, Long>> applicationsByStatusPerEnterprise;
    private Map<String, Long> applicationsPerEnterprise;
    private Map<String, Long> selectionCountPerEnterprise;
    private Map<String, Long> rejectionCountPerEnterprise;
    private Map<String, Long> interviewCountPerEnterprise;
    private Map<String, Long> forwardedCountPerEnterprise;
    private Map<String, Double> averageProcessingTimePerEnterprise;
}
