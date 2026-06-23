package com.zencube.registry.profile.mapper;

import com.zencube.registry.profile.dto.response.SkillResponse;
import com.zencube.registry.profile.entity.StudentSkill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {
    public SkillResponse toResponse(StudentSkill skill) {
        if (skill == null) return null;
        return SkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory())
                .build();
    }
}
