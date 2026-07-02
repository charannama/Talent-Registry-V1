package com.zencube.registry.profile.controller;

import com.zencube.registry.common.response.ApiResponse;
import com.zencube.registry.profile.dto.response.ProjectResponse;
import com.zencube.registry.profile.service.ProjectService;
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
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/me/projects")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects() {
        List<ProjectResponse> projects = projectService.getMyProjects();
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projects));
    }

    @GetMapping("/user/{userId}/projects")
    @PreAuthorize("hasAnyRole('HR_STAFF','ADMIN')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByUserId(@PathVariable UUID userId) {
        List<ProjectResponse> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved successfully", projects));
    }
}
