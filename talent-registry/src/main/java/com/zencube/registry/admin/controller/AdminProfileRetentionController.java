package com.zencube.registry.admin.controller;

import com.zencube.registry.admin.dto.response.RetentionStatusResponse;
import com.zencube.registry.admin.service.ProfileRetentionService;
import com.zencube.registry.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/profile")
@RequiredArgsConstructor
public class AdminProfileRetentionController {

    private final ProfileRetentionService retentionService;

    @GetMapping("/{profileId}/retention")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetentionStatusResponse>> checkRetention(
            @PathVariable UUID profileId) {
        RetentionStatusResponse response = retentionService.checkRetention(profileId);
        return ResponseEntity.ok(ApiResponse.success("Retention status retrieved", response));
    }

    @DeleteMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @PathVariable UUID profileId) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        retentionService.deleteProfile(profileId, adminEmail);
        return ResponseEntity.ok(ApiResponse.success("Profile logically deleted", null));
    }
}
