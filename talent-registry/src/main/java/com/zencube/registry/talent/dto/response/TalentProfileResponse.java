package com.zencube.registry.talent.dto.response;

import com.zencube.registry.profile.dto.response.ProjectResponse;
import com.zencube.registry.profile.dto.response.SkillResponse;
import com.zencube.registry.profile.dto.response.WorkExperienceResponse;
import com.zencube.registry.profile.enums.EligibilityLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TalentProfileResponse {
    
    private UUID profileId;
    private String name;
    private String avatarUrl;
    private String institution;
    private String discipline;
    private Integer graduationYear;
    private Double gpa;
    private String coursework;
    private Boolean fullTimeReady;
    private Boolean internshipReady;
    private Boolean remotePreference;
    private EligibilityLevel eligibilityLevel;
    private Boolean profileVisible;
    private Boolean suspended;
    private String suspensionReason;
    
    private List<SkillResponse> skills;
    private List<ProjectResponse> projects;
    private List<WorkExperienceResponse> workExperiences;

}
