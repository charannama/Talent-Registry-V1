package com.zencube.registry.profile.service;

import com.zencube.registry.profile.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    List<ProjectResponse> getMyProjects();

    List<ProjectResponse> getProjectsByUserId(UUID userId);
}
