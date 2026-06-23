package com.zencube.registry.profile.service;

import com.zencube.registry.profile.dto.response.WorkExperienceResponse;

import java.util.List;
import java.util.UUID;

public interface WorkExperienceService {

    List<WorkExperienceResponse> getMyExperiences();

    List<WorkExperienceResponse> getExperiencesByUserId(UUID userId);
}
