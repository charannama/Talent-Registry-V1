package com.zencube.registry.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionMetricsResponse {
    private double overallConversionRate; // Selected / Total
    private double selectionRate;         // Selected / Total processed
    private double rejectionRate;         // Rejected / Total processed
    private double interviewRate;         // Interviewed / Total
    private double averageProcessingTimeDays; 
}
