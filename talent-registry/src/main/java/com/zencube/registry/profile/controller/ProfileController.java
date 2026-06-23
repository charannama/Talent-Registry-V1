package com.zencube.registry.profile.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.profile.dto.response.ProfileResponse;
import com.zencube.registry.profile.service.ProfileService;
import com.zencube.registry.profile.service.ProfileSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileSyncService syncService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        ProfileResponse response = profileService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PostMapping("/me/sync")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ProfileResponse>> syncProfile() {
        ProfileResponse response = syncService.syncProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile sync triggered successfully", response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('PROFILE_VIEW_ALL')")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfileByUserId(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID userId,
            jakarta.servlet.http.HttpServletRequest request) {
        ProfileResponse response = profileService.getProfileByUserId(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }
}
