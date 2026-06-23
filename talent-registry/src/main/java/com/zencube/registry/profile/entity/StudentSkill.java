package com.zencube.registry.profile.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private StudentProfile profile;

    private String skillName;

    @Enumerated(EnumType.STRING)
    private SkillCategory category;
}
