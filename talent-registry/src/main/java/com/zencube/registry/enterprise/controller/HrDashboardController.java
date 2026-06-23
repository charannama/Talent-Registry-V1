package com.zencube.registry.enterprise.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.enterprise.dto.response.EnterpriseDashboardMetricsResponse;
import com.zencube.registry.enterprise.enums.DateRangeFilter;
import com.zencube.registry.enterprise.service.HrDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/hr/dashboard")
@RequiredArgsConstructor
@Tag(name = "HR Dashboard API", description = "Endpoints for HR administrative dashboard metrics")
public class HrDashboardController {

    private final HrDashboardService dashboardService;

    @Operation(summary = "Get Enterprise Metrics", description = "Retrieves aggregated onboarding metrics for enterprises")
    @GetMapping("/enterprise-metrics")
    @PreAuthorize("hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EnterpriseDashboardMetricsResponse>> getEnterpriseMetrics(
            @Parameter(description = "Date range filter") 
            @RequestParam(defaultValue = "ALL_TIME") DateRangeFilter filter,
            @Parameter(description = "Custom start date for CUSTOM filter") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant customStart,
            @Parameter(description = "Custom end date for CUSTOM filter") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant customEnd) {
        
        EnterpriseDashboardMetricsResponse metrics = dashboardService.getEnterpriseMetrics(filter, customStart, customEnd);
        return ResponseEntity.ok(ApiResponse.success("Enterprise metrics retrieved successfully", metrics));
    }
}
