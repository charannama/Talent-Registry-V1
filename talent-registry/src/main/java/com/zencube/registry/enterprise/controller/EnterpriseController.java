package com.zencube.registry.enterprise.controller;

import com.zencube.registry.enterprise.dto.request.CreateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.request.UpdateEnterpriseRequest;
import com.zencube.registry.enterprise.dto.request.EnterpriseSignupRequest;
import com.zencube.registry.enterprise.dto.response.EnterpriseResponse;
import com.zencube.registry.enterprise.dto.response.EnterpriseSignupResponse;
import com.zencube.registry.enterprise.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enterprises")
@RequiredArgsConstructor
@Tag(name = "Enterprise Registration", description = "Endpoints for enterprise signup and management")
public class EnterpriseController {

    private final EnterpriseService service;

    @Operation(summary = "Register a new Enterprise Account and User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enterprise registered successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = EnterpriseSignupResponse.class),
                    examples = @ExampleObject(value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"userId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"PENDING_HR_REVIEW\",\n  \"message\": \"Enterprise registered successfully. Please check your email for verification.\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or personal email domain",
                    content = @Content(examples = @ExampleObject(value = "{\n  \"status\": 400,\n  \"error\": \"INVALID_ENTERPRISE_EMAIL\",\n  \"message\": \"Please use your company email address\"\n}"))),
            @ApiResponse(responseCode = "409", description = "Email or Company Name already registered",
                    content = @Content(examples = @ExampleObject(value = "{\n  \"status\": 409,\n  \"error\": \"DUPLICATE_COMPANY\",\n  \"message\": \"Company already registered\"\n}")))
    })
    @PostMapping("/signup")
    public ResponseEntity<EnterpriseSignupResponse> signup(
            @Valid @RequestBody EnterpriseSignupRequest request) {
        return ResponseEntity.ok(service.signup(request));
    }

    @PostMapping
    public ResponseEntity<EnterpriseResponse> register(
            @Valid @RequestBody CreateEnterpriseRequest request) {
        return ResponseEntity.ok(service.registerEnterprise(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@enterpriseSecurity.isOwnerOrAdmin(#id)")
    public ResponseEntity<EnterpriseResponse> getEnterprise(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getEnterprise(id));
    }

    @Operation(summary = "Update My Enterprise Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(examples = @ExampleObject(value = "{\n  \"status\": 400,\n  \"error\": \"BAD_REQUEST\",\n  \"message\": \"Company name is required\"\n}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not Approved or Not Owner", content = @Content(examples = @ExampleObject(value = "{\n  \"status\": 403,\n  \"error\": \"ENTERPRISE_NOT_APPROVED\",\n  \"message\": \"Enterprise is not approved\"\n}"))),
            @ApiResponse(responseCode = "409", description = "Duplicate Company or Domain", content = @Content(examples = @ExampleObject(value = "{\n  \"status\": 409,\n  \"error\": \"DUPLICATE_COMPANY\",\n  \"message\": \"Company already registered\"\n}")))
    })
    @PutMapping("/my")
    @PreAuthorize("@enterpriseSecurity.isCurrentEnterpriseOwner()")
    public ResponseEntity<EnterpriseResponse> updateMyProfile(
            @Valid @RequestBody UpdateEnterpriseRequest request) {
        return ResponseEntity.ok(service.updateMyProfile(request));
    }

    @Operation(summary = "Get My Enterprise Registration Status", description = "Checks the current onboarding and active status of the enterprise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.zencube.registry.enterprise.dto.response.EnterpriseStatusResponse.class),
                            examples = {
                                    @ExampleObject(name = "PENDING", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"PENDING\",\n  \"statusMessage\": \"Your application is under review (1-3 business days)\",\n  \"accountActive\": false\n}"),
                                    @ExampleObject(name = "APPROVED", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"APPROVED\",\n  \"statusMessage\": \"Your account is active\",\n  \"accountActive\": true\n}"),
                                    @ExampleObject(name = "REJECTED", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"REJECTED\",\n  \"statusMessage\": \"Your application was rejected\",\n  \"rejectionReason\": \"Invalid documentation\",\n  \"accountActive\": false\n}")
                            })),
            @ApiResponse(responseCode = "404", description = "Enterprise account not found")
    })
    @GetMapping("/my/status")
    @PreAuthorize("@enterpriseSecurity.isCurrentEnterpriseOwner()")
    public ResponseEntity<com.zencube.registry.enterprise.dto.response.EnterpriseStatusResponse> getMyRegistrationStatus() {
        return ResponseEntity.ok(service.getMyRegistrationStatus());
    }

    @Operation(summary = "Get Enterprise Dashboard Initialization payload", description = "Enterprise Access Gateway endpoint. Checks current status and provides boolean capabilities for the frontend dashboard.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard loaded",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.zencube.registry.enterprise.dto.response.EnterpriseDashboardResponse.class),
                            examples = {
                                    @ExampleObject(name = "APPROVED", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"APPROVED\",\n  \"statusMessage\": \"Your account is active.\",\n  \"canPostOpenings\": true,\n  \"canSearchTalent\": true,\n  \"canManageJobs\": true\n}"),
                                    @ExampleObject(name = "PENDING", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"PENDING_HR_REVIEW\",\n  \"statusMessage\": \"Your application is under review (1-3 business days).\",\n  \"canPostOpenings\": false,\n  \"canSearchTalent\": false,\n  \"canManageJobs\": false\n}"),
                                    @ExampleObject(name = "REJECTED", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"REJECTED\",\n  \"statusMessage\": \"Your application was rejected.\",\n  \"canPostOpenings\": false,\n  \"canSearchTalent\": false,\n  \"canManageJobs\": false\n}"),
                                    @ExampleObject(name = "SUSPENDED", value = "{\n  \"enterpriseId\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n  \"companyName\": \"ZenCube Tech\",\n  \"status\": \"SUSPENDED\",\n  \"statusMessage\": \"Your account is suspended.\",\n  \"canPostOpenings\": false,\n  \"canSearchTalent\": false,\n  \"canManageJobs\": false\n}")
                            })),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Incorrect role"),
            @ApiResponse(responseCode = "404", description = "Enterprise account not found")
    })
    @GetMapping("/my/dashboard")
    @PreAuthorize("@enterpriseSecurity.canAccessMyDashboard()")
    public ResponseEntity<com.zencube.registry.enterprise.dto.response.EnterpriseDashboardResponse> getMyDashboard() {
        return ResponseEntity.ok(service.getMyDashboard());
    }
}
