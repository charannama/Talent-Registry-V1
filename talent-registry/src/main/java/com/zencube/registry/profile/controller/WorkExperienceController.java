package com.zencube.registry.profile.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.profile.dto.response.WorkExperienceResponse;
import com.zencube.registry.profile.service.WorkExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;

    @GetMapping("/me/experiences")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getMyExperiences() {
        List<WorkExperienceResponse> experiences = workExperienceService.getMyExperiences();
        return ResponseEntity.ok(ApiResponse.success("Work experiences retrieved successfully", experiences));
    }

    @GetMapping("/user/{userId}/experiences")
    @PreAuthorize("hasAnyRole('HR_STAFF','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getUserExperiences(@PathVariable UUID userId) {
        List<WorkExperienceResponse> experiences = workExperienceService.getExperiencesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Work experiences retrieved successfully", experiences));
    }
}
