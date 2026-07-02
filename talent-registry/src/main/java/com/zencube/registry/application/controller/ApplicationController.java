package com.zencube.registry.application.controller;

import com.zencube.registry.application.dto.response.ApplicationPageResponse;
import com.zencube.registry.application.dto.response.PendingApplicationResponse;
import com.zencube.registry.application.service.ApplicationService;
import com.zencube.registry.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('APPLICATION_VIEW_PENDING') or hasRole('HR_STAFF') or hasRole('ENTERPRISE_RECRUITER')")
    public ResponseEntity<ApiResponse<ApplicationPageResponse<PendingApplicationResponse>>> getPendingApplications(
            @RequestParam(required = false, defaultValue = "APPLIED") String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "appliedAt") String sort,
            @RequestParam(required = false, defaultValue = "ASC") String direction) {
        
        ApplicationPageResponse<PendingApplicationResponse> response = applicationService.getPendingReviewQueue(status, search, page, size, sort, direction);
        return ResponseEntity.ok(ApiResponse.success("Pending applications retrieved successfully", response));
    }

    @PostMapping("/jobs/{jobId}/apply")
    @PreAuthorize("hasAuthority('APPLICATION_CREATE') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> applyToOpening(@PathVariable UUID jobId, 
                                                            @org.springframework.security.core.annotation.AuthenticationPrincipal com.zencube.registry.security.model.CustomUserDetails userDetails, 
                                                            HttpServletRequest request) {
        UUID currentUserId = userDetails.getUserId();
        applicationService.applyToOpening(jobId, currentUserId);
        
        log.info("AUDIT - Application Created: StudentID={}, OpeningID={}, Timestamp={}, IPAddress={}", 
                 currentUserId, jobId, java.time.Instant.now(), request.getRemoteAddr());
                 
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success("Successfully applied to opening", null));
    }

    @PostMapping("/{id}/forward")
    @PreAuthorize("hasAuthority('APPLICATION_UPDATE') or hasRole('HR_STAFF')")
    public ResponseEntity<ApiResponse<Void>> forwardApplication(@PathVariable UUID id, Principal principal, HttpServletRequest request) {
        applicationService.updateApplicationStatus(id, com.zencube.registry.common.enums.ApplicationStatus.FORWARDED);
        
        log.info("AUDIT - Application Forwarded: UserID={}, ApplicationID={}, Timestamp={}, IPAddress={}", 
                 principal.getName(), id, java.time.Instant.now(), request.getRemoteAddr());
                 
        return ResponseEntity.ok(ApiResponse.success("Application forwarded successfully", null));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('APPLICATION_UPDATE') or hasRole('ENTERPRISE_RECRUITER') or hasRole('ENTERPRISE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateApplicationStatus(
            @PathVariable UUID id, 
            @RequestParam com.zencube.registry.common.enums.ApplicationStatus newStatus, 
            @RequestParam(required = false) String notes,
            Principal principal, 
            HttpServletRequest request) {
        applicationService.updateApplicationStatus(id, newStatus);
        
        log.info("AUDIT - Application Status Updated: UserID={}, ApplicationID={}, NewStatus={}, Notes={}, Timestamp={}, IPAddress={}", 
                 principal.getName(), id, newStatus, notes, java.time.Instant.now(), request.getRemoteAddr());
                 
        return ResponseEntity.ok(ApiResponse.success("Application status updated to " + newStatus, null));
    }
}
