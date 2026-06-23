package com.zencube.registry.profile.dto.response;

import com.zencube.registry.profile.enums.SkillCategory;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {
    private UUID id;
    private String skillName;
    private SkillCategory category;
}
