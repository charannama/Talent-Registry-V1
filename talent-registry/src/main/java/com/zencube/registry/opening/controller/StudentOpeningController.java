package com.zencube.registry.opening.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.opening.dto.request.OpeningSearchCriteria;
import com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse;
import com.zencube.registry.opening.service.OpeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student/openings")
@RequiredArgsConstructor
@Tag(name = "Student Job Discovery API", description = "Endpoints for students to search and filter active job openings")
public class StudentOpeningController {

    private final OpeningService openingService;

    @Operation(summary = "Search Job Openings", description = "Advanced search and filter for live job openings.")
    @GetMapping
    @PreAuthorize("hasAuthority('OPENING_VIEW_ALL') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PaginatedOpeningSummaryResponse>> searchOpenings(
            @Parameter(description = "Search filters") OpeningSearchCriteria criteria,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {

        PaginatedOpeningSummaryResponse response = openingService.searchOpenings(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Job openings retrieved successfully", response));
    }
}
