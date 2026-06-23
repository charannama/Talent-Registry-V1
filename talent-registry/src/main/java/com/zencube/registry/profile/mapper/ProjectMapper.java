package com.zencube.registry.profile.mapper;

import com.zencube.registry.profile.dto.response.ProjectResponse;
import com.zencube.registry.profile.entity.StudentProject;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(StudentProject project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .projectType(project.getProjectType())
                .domain(project.getDomain())
                .completionDate(project.getCompletionDate())
                .rubricScore(project.getRubricScore())
                .mentorFeedback(project.getMentorFeedback())
                .technologiesUsed(project.getTechnologiesUsed())
                .repositoryUrl(project.getRepositoryUrl())
                .liveUrl(project.getLiveUrl())
                .build();
    }
}
