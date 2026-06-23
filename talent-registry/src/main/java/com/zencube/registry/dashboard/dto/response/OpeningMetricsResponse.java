package com.zencube.registry.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpeningMetricsResponse {
    private List<OpeningDistributionDto> openingDistributions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpeningDistributionDto {
        private String openingName;
        private String enterpriseName;
        private long applicationCount;
        private double percentage;
    }
}
