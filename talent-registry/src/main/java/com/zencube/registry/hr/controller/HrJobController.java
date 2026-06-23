package com.zencube.registry.hr.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.service.OpeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/jobs")
@RequiredArgsConstructor
@Tag(name = "HR Job Openings API", description = "Endpoints for HR to manage job openings")
public class HrJobController {

    private final OpeningService openingService;

    @Operation(summary = "Approve Job Opening (HR)", description = "Allows HR staff to approve a pending job opening and make it LIVE.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opening approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid state transition or not pending"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Opening not found")
    })
    @PostMapping("/{jobId}/approve")
    @PreAuthorize("hasAuthority('OPENING_APPROVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> approveJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success("Job opening approved and is now LIVE", openingService.approveOpening(jobId)));
    }

    @Operation(summary = "Reject Job Opening (HR)", description = "Allows HR staff to reject a pending job opening.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opening rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid state transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Opening not found")
    })
    @PostMapping("/{jobId}/reject")
    @PreAuthorize("hasAuthority('OPENING_APPROVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> rejectJob(@PathVariable UUID jobId, @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid com.zencube.registry.opening.dto.request.RejectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job opening rejected", openingService.rejectOpening(jobId, request)));
    }

    @Operation(summary = "List Pending Job Openings (HR)", description = "Allows HR staff to list all pending job openings.")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('OPENING_VIEW_ALL')")
    public ResponseEntity<ApiResponse<com.zencube.registry.opening.dto.response.PaginatedOpeningResponse>> getPendingJobs(
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Pending job openings retrieved successfully", openingService.listPendingOpenings(pageable)));
    }

    @Operation(summary = "Request Revision for Job Opening (HR)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Revision requested successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid state transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Opening not found")
    })
    @PostMapping("/{jobId}/request-revision")
    @PreAuthorize("hasAuthority('OPENING_APPROVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> requestRevision(@PathVariable UUID jobId, @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid com.zencube.registry.opening.dto.request.RequestRevisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Revision requested", openingService.requestRevision(jobId, request)));
    }
}
