package com.zencube.registry.profile.mapper;

import com.zencube.registry.profile.dto.response.WorkExperienceResponse;
import com.zencube.registry.profile.entity.WorkExperience;
import org.springframework.stereotype.Component;

@Component
public class WorkExperienceMapper {

    public WorkExperienceResponse toResponse(WorkExperience experience) {
        if (experience == null) {
            return null;
        }

        return WorkExperienceResponse.builder()
                .id(experience.getId())
                .jobTitle(experience.getJobTitle())
                .companyName(experience.getCompanyName())
                .location(experience.getLocation())
                .employmentType(experience.getEmploymentType())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .currentlyWorking(experience.getCurrentlyWorking())
                .description(experience.getDescription())
                .keyResponsibilities(experience.getKeyResponsibilities())
                .build();
    }
}
