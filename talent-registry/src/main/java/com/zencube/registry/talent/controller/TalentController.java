package com.zencube.registry.talent.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.talent.dto.request.TalentSearchRequest;
import com.zencube.registry.talent.dto.response.TalentProfileResponse;
import com.zencube.registry.talent.dto.response.TalentSearchResponse;
import com.zencube.registry.talent.service.TalentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/talent")
@RequiredArgsConstructor
public class TalentController {

    private final TalentService talentService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<TalentSearchResponse>> searchTalent(
            TalentSearchRequest request,
            Pageable pageable) {
        TalentSearchResponse response = talentService.searchTalent(request, pageable);
        return ResponseEntity.ok(ApiResponse.success("Talent search successful", response));
    }

    @GetMapping("/profile/{profileId}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<TalentProfileResponse>> getProfile(
            @PathVariable UUID profileId,
            HttpServletRequest request) {
        TalentProfileResponse response = talentService.getProfile(profileId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PostMapping("/profile/{profileId}/suspend")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> suspendProfile(
            @PathVariable UUID profileId,
            @jakarta.validation.Valid @RequestBody com.zencube.registry.talent.dto.request.SuspendProfileRequest suspendRequest) {
        String suspendedBy = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        talentService.suspendProfile(profileId, suspendRequest.getReason(), suspendedBy);
        return ResponseEntity.ok(ApiResponse.success("Profile suspended successfully", null));
    }

    @PostMapping("/profile/{profileId}/reinstate")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reinstateProfile(
            @PathVariable UUID profileId) {
        talentService.reinstateProfile(profileId);
        return ResponseEntity.ok(ApiResponse.success("Profile reinstated successfully", null));
    }
}
