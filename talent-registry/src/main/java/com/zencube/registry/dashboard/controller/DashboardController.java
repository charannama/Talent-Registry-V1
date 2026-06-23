package com.zencube.registry.dashboard.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.dashboard.dto.response.DashboardAnalyticsResponse;
import com.zencube.registry.dashboard.dto.response.EnterpriseMetricsResponse;
import com.zencube.registry.dashboard.dto.response.OpeningMetricsResponse;
import com.zencube.registry.dashboard.dto.response.PipelineMetricsResponse;
import com.zencube.registry.dashboard.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard/applications")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private void logAudit(String endpoint, Principal principal, HttpServletRequest request) {
        String userId = principal != null ? principal.getName() : "anonymous";
        String ipAddress = request.getRemoteAddr();
        log.info("AUDIT - Dashboard Viewed: Endpoint={}, UserID={}, Timestamp={}, IPAddress={}", endpoint, userId, java.time.Instant.now(), ipAddress);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<DashboardAnalyticsResponse>> getDashboardAnalytics(Principal principal, HttpServletRequest request) {
        logAudit("/api/v1/dashboard/applications", principal, request);
        DashboardAnalyticsResponse response = dashboardService.getDashboardAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Dashboard analytics retrieved successfully", response));
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PipelineMetricsResponse>> getPipelineMetrics(Principal principal, HttpServletRequest request) {
        logAudit("/api/v1/dashboard/applications/pipeline", principal, request);
        PipelineMetricsResponse response = dashboardService.getPipelineMetrics();
        return ResponseEntity.ok(ApiResponse.success("Pipeline metrics retrieved successfully", response));
    }

    @GetMapping("/enterprises")
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<EnterpriseMetricsResponse>> getEnterpriseMetrics(Principal principal, HttpServletRequest request) {
        logAudit("/api/v1/dashboard/applications/enterprises", principal, request);
        EnterpriseMetricsResponse response = dashboardService.getEnterpriseMetrics();
        return ResponseEntity.ok(ApiResponse.success("Enterprise metrics retrieved successfully", response));
    }

    @GetMapping("/openings")
    @PreAuthorize("hasAnyRole('HR_STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<OpeningMetricsResponse>> getOpeningMetrics(Principal principal, HttpServletRequest request) {
        logAudit("/api/v1/dashboard/applications/openings", principal, request);
        OpeningMetricsResponse response = dashboardService.getOpeningMetrics();
        return ResponseEntity.ok(ApiResponse.success("Opening metrics retrieved successfully", response));
    }
}
