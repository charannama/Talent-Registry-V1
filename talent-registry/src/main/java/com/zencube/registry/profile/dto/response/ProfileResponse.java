package com.zencube.registry.profile.dto.response;

import com.zencube.registry.profile.enums.EligibilityLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
public class ProfileResponse {
    private UUID profileId;
    private String name;
    private String email;
    private String avatarUrl;
    private String institution;
    private String discipline;
    private Integer graduationYear;
    private Double gpa;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private EligibilityLevel eligibilityLevel;
    private Instant lastSyncAt;
    private Boolean profileVisible;
    private Boolean suspended;
    private String suspensionReason;
    private java.util.List<SkillResponse> skills;
    private java.util.List<ProjectResponse> projects;
    private java.util.List<WorkExperienceResponse> workExperiences;
}
