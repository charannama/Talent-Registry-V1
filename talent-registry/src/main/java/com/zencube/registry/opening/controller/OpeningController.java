package com.zencube.registry.opening.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.opening.dto.request.CreateOpeningRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.service.OpeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/openings")
@RequiredArgsConstructor
@Tag(name = "Job Opening API", description = "Endpoints for managing enterprise job openings")
public class OpeningController {

    private final OpeningService openingService;

    @Operation(summary = "Create Draft Job Opening", description = "Creates a new job opening in DRAFT status. Restricts operation to the enterprise owner only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Job opening successfully created in DRAFT status",
                    content = @Content(schema = @Schema(implementation = OpeningResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request arguments or invalid enterprise state"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Unauthorized user (ownership validation failed)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict: duplicate job opening title within this enterprise")
    })
    @PostMapping
    @PreAuthorize("@enterpriseSecurity.isOwner(#request.enterpriseId)")
    public ResponseEntity<ApiResponse<OpeningResponse>> createOpening(@Valid @RequestBody CreateOpeningRequest request) {
        OpeningResponse response = openingService.createOpening(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Job opening successfully created in DRAFT status", response));
    }

    @Operation(summary = "Get Job Opening", description = "Retrieves details of a specific job opening by its ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opening details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job opening not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OpeningResponse>> getOpening(@PathVariable UUID id) {
        OpeningResponse response = openingService.getOpening(id);
        return ResponseEntity.ok(ApiResponse.success("Job opening details retrieved successfully", response));
    }

    @Operation(summary = "List Enterprise Openings", description = "Retrieves all job openings (non-deleted) for a specific enterprise.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Openings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Enterprise not found")
    })
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<ApiResponse<List<OpeningResponse>>> listOpenings(@PathVariable UUID enterpriseId) {
        List<OpeningResponse> response = openingService.listOpenings(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success("Enterprise openings retrieved successfully", response));
    }

    @Operation(summary = "Submit a Job Opening for HR Approval", 
               description = "Transitions a draft opening to PENDING_HR_APPROVAL status after validating required fields.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opening submitted successfully",
                    content = @Content(schema = @Schema(implementation = OpeningResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid state, incomplete fields, or past deadline"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Enterprise not approved or ownership violation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Opening not found")
    })
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('OPENING_SUBMIT')")
    public ResponseEntity<ApiResponse<OpeningResponse>> submitOpening(@PathVariable UUID id) {
        OpeningResponse response = openingService.submitOpening(id);
        return ResponseEntity.ok(ApiResponse.success("Opening submitted successfully for HR approval", response));
    }

    @Operation(summary = "Approve Job Opening", description = "Allows HR to approve a pending job opening.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Opening approved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid state transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Opening not found")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('OPENING_APPROVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> approveOpening(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Job opening approved and is now LIVE", openingService.approveOpening(id)));
    }

    @Operation(summary = "Update Draft Job Opening")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPENING_UPDATE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> updateDraft(@PathVariable UUID id, @Valid @RequestBody com.zencube.registry.opening.dto.request.UpdateOpeningRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Draft updated successfully", openingService.updateDraft(id, request)));
    }

    @Operation(summary = "Reject Job Opening")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('OPENING_APPROVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> rejectOpening(@PathVariable UUID id, @Valid @RequestBody com.zencube.registry.opening.dto.request.RejectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Opening rejected", openingService.rejectOpening(id, request)));
    }

    @Operation(summary = "Close Job Opening")
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('OPENING_CLOSE')")
    public ResponseEntity<ApiResponse<com.zencube.registry.opening.dto.response.CloseOpeningResponse>> closeOpening(@PathVariable UUID id, @Valid @RequestBody(required = false) com.zencube.registry.opening.dto.request.CloseOpeningRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Opening closed", openingService.closeOpening(id, request)));
    }

    @Operation(summary = "Archive Job Opening")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('OPENING_ARCHIVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> archiveOpening(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Opening archived", openingService.archiveOpening(id)));
    }

    @Operation(summary = "Request Revision for Job Opening")
    @PostMapping("/{id}/request-revision")
    @PreAuthorize("hasAuthority('OPENING_APPROVE')")
    public ResponseEntity<ApiResponse<OpeningResponse>> requestRevision(@PathVariable UUID id, @Valid @RequestBody com.zencube.registry.opening.dto.request.RequestRevisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Revision requested", openingService.requestRevision(id, request)));
    }

    @Operation(summary = "Resubmit Job Opening")
    @PostMapping("/{id}/resubmit")
    @PreAuthorize("hasAuthority('OPENING_SUBMIT')")
    public ResponseEntity<ApiResponse<com.zencube.registry.opening.dto.response.ResubmitOpeningResponse>> resubmitOpening(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Opening resubmitted", openingService.resubmitOpening(id)));
    }

    @Operation(summary = "List My Enterprise Openings")
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('OPENING_VIEW')")
    public ResponseEntity<ApiResponse<com.zencube.registry.opening.dto.response.PaginatedOpeningResponse>> getMyOpenings(
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("My enterprise openings retrieved successfully", openingService.listMyEnterpriseOpenings(pageable)));
    }

    @Operation(summary = "Browse Live Job Openings", description = "Public endpoint to browse live job openings, ordered by featured and published date.")
    @GetMapping
    public ResponseEntity<ApiResponse<com.zencube.registry.opening.dto.response.PaginatedOpeningSummaryResponse>> browseOpenings(
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Live openings retrieved successfully", openingService.browseOpenings(pageable)));
    }
}
