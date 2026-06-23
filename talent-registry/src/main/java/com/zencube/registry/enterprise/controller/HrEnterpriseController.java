package com.zencube.registry.enterprise.controller;

import com.zencube.registry.enterprise.dto.response.EnterpriseApprovalResponse;
import com.zencube.registry.enterprise.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/enterprises")
@RequiredArgsConstructor
@Tag(name = "HR Enterprise Management", description = "Endpoints for HR to review, approve, and suspend enterprise accounts")
public class HrEnterpriseController {

    private final EnterpriseService service;

    @Operation(summary = "List Enterprises", description = "Get a paginated list of enterprise accounts, filtered by status and company name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ENTERPRISE_MANAGE') or hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<com.zencube.registry.enterprise.dto.response.EnterpriseSummaryResponse>> getEnterprises(
            @RequestParam(required = false) com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus status,
            @RequestParam(required = false) String companyName,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(service.getEnterprises(status, companyName, pageable));
    }

    @Operation(summary = "View Enterprise Details", description = "Fetch complete details of an enterprise account by HR.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enterprise details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Enterprise not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ENTERPRISE_MANAGE') or hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<com.zencube.registry.enterprise.dto.response.HrEnterpriseDetailResponse> getEnterpriseDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getEnterpriseDetailsForHr(id));
    }
    @Operation(summary = "Approve Enterprise Registration", description = "Transitions a PENDING enterprise to APPROVED status, enabling their dashboard access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enterprise successfully approved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EnterpriseApprovalResponse.class),
                            examples = {
                                    @ExampleObject(name = "Success", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"APPROVED\",\n  \"accountActive\": true,\n  \"approvedAt\": \"2026-06-17T12:00:00Z\",\n  \"approvedBy\": \"11223344-5566-7788-99aa-bbccddeeff00\",\n  \"message\": \"Enterprise successfully approved\"\n}")
                            })),
            @ApiResponse(responseCode = "400", description = "Invalid state transition (e.g. already approved)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not HR or Admin"),
            @ApiResponse(responseCode = "404", description = "Enterprise account not found")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ENTERPRISE_MANAGE') or hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<EnterpriseApprovalResponse> approveEnterprise(@PathVariable UUID id) {
        return ResponseEntity.ok(service.approveEnterprise(id));
    }

    @Operation(summary = "Reject Enterprise Registration", description = "Transitions a PENDING enterprise to REJECTED status. Requires a mandatory rejection reason.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enterprise successfully rejected",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.zencube.registry.enterprise.dto.response.EnterpriseRejectionResponse.class),
                            examples = {
                                    @ExampleObject(name = "Success", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"REJECTED\",\n  \"accountActive\": false,\n  \"rejectionReason\": \"Invalid documentation\",\n  \"rejectedAt\": \"2026-06-17T12:00:00Z\",\n  \"rejectedBy\": \"11223344-5566-7788-99aa-bbccddeeff00\",\n  \"message\": \"Enterprise successfully rejected\"\n}")
                            })),
            @ApiResponse(responseCode = "400", description = "Invalid state transition or missing reason"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not HR or Admin"),
            @ApiResponse(responseCode = "404", description = "Enterprise account not found")
    })
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ENTERPRISE_MANAGE') or hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<com.zencube.registry.enterprise.dto.response.EnterpriseRejectionResponse> rejectEnterprise(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(service.rejectEnterprise(id, reason));
    }

    @Operation(summary = "Suspend Enterprise Account", description = "Transitions an APPROVED enterprise to SUSPENDED status. Requires a mandatory suspension reason.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enterprise successfully suspended",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.zencube.registry.enterprise.dto.response.EnterpriseSuspensionResponse.class),
                            examples = {
                                    @ExampleObject(name = "Success", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"SUSPENDED\",\n  \"accountActive\": false,\n  \"suspensionReason\": \"Violation of Terms of Service\",\n  \"suspendedAt\": \"2026-06-17T12:00:00Z\",\n  \"message\": \"Enterprise successfully suspended\"\n}")
                            })),
            @ApiResponse(responseCode = "400", description = "Invalid state transition or missing reason"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not HR or Admin"),
            @ApiResponse(responseCode = "404", description = "Enterprise account not found")
    })
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ENTERPRISE_MANAGE') or hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<com.zencube.registry.enterprise.dto.response.EnterpriseSuspensionResponse> suspendEnterprise(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(service.suspendEnterprise(id, reason));
    }

    @Operation(summary = "Reactivate Enterprise Account", description = "Transitions a SUSPENDED enterprise back to APPROVED status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enterprise successfully reactivated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.zencube.registry.enterprise.dto.response.ReactivateEnterpriseResponse.class),
                            examples = {
                                    @ExampleObject(name = "Success", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"APPROVED\",\n  \"accountActive\": true,\n  \"reactivatedAt\": \"2026-06-17T12:00:00Z\",\n  \"reactivatedBy\": \"11223344-5566-7788-99aa-bbccddeeff00\",\n  \"message\": \"Enterprise successfully reactivated\"\n}")
                            })),
            @ApiResponse(responseCode = "400", description = "Invalid state transition"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not HR or Admin"),
            @ApiResponse(responseCode = "404", description = "Enterprise account not found")
    })
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ENTERPRISE_MANAGE') or hasRole('HR_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<com.zencube.registry.enterprise.dto.response.ReactivateEnterpriseResponse> reactivateEnterprise(@PathVariable UUID id) {
        return ResponseEntity.ok(service.reactivateEnterprise(id));
    }
}
