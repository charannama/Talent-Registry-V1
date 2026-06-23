package com.zencube.registry.enterprise.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.enterprise.service.EnterpriseDashboardMetricsService;
import com.zencube.registry.opening.dto.request.CloseOpeningRequest;
import com.zencube.registry.opening.dto.response.CloseOpeningResponse;
import com.zencube.registry.opening.service.OpeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enterprise/jobs")
@RequiredArgsConstructor
@Tag(name = "Enterprise Job Management API", description = "Endpoints for enterprise owners to manage their job openings")
public class EnterpriseJobController {

    private final OpeningService openingService;
    private final EnterpriseDashboardMetricsService dashboardMetricsService;
    private final com.zencube.registry.application.service.ApplicationService applicationService;

    @Operation(summary = "Close Job Opening (Enterprise Owner)", description = "Allows an enterprise owner to close one of their live job openings.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opening closed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid state transition or enterprise not approved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Ownership violation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Opening not found")
    })
    @PostMapping("/{jobId}/close")
    @PreAuthorize("hasAuthority('OPENING_CLOSE')")
    public ResponseEntity<ApiResponse<CloseOpeningResponse>> closeJob(@PathVariable UUID jobId, @Valid @RequestBody(required = false) CloseOpeningRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job opening closed", openingService.closeOpening(jobId, request)));
    }

    @Operation(summary = "Get Enterprise Job Metrics", description = "Returns dashboard metrics for job openings.")
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('OPENING_VIEW')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success("Metrics retrieved successfully", dashboardMetricsService.getDashboardMetrics()));
    }

    @Operation(summary = "Get Forwarded Applications", description = "View applications forwarded to a job opening.")
    @GetMapping("/{jobId}/applications")
    @PreAuthorize("hasAuthority('APPLICATION_VIEW_FORWARDED')")
    public ResponseEntity<ApiResponse<com.zencube.registry.application.dto.response.EnterpriseApplicationPageResponse<com.zencube.registry.application.dto.response.EnterpriseApplicationResponse>>> getForwardedApplications(
            @PathVariable("jobId") UUID jobId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "lastStageUpdatedAt") String sort,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction,
            java.security.Principal principal
    ) {
        UUID currentUserId = UUID.fromString(principal.getName());
        com.zencube.registry.application.dto.response.EnterpriseApplicationPageResponse<com.zencube.registry.application.dto.response.EnterpriseApplicationResponse> response = 
                this.applicationService.getForwardedApplicationsForEnterprise(
                jobId, search, status, page, size, sort, direction, currentUserId
        );
        return ResponseEntity.ok(ApiResponse.success("Forwarded applications retrieved successfully", response));
    }
}
