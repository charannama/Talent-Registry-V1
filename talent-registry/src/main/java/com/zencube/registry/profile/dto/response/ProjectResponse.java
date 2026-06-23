package com.zencube.registry.profile.dto.response;

import com.zencube.registry.profile.enums.ProjectType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private UUID id;

    private String projectName;

    private String description;

    private ProjectType projectType;

    private String domain;

    private Instant completionDate;

    private Integer rubricScore;

    private String mentorFeedback;

    private String technologiesUsed;

    private String repositoryUrl;

    private String liveUrl;
}
