package com.zencube.registry.talent.dto.response;

import com.zencube.registry.profile.enums.ProjectType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TalentProfileSummaryResponse {

    private UUID profileId;
    private String name;
    private String avatarUrl;
    private String institution;
    private String discipline;
    private Integer graduationYear;
    private List<String> skills;
    private ProjectType highestProjectType;
    private Long profileViews;

}
