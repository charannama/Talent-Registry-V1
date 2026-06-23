package com.zencube.registry.profile.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.enums.ProjectType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "student_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "profile_id",
            nullable = false
    )
    private StudentProfile profile;

    @Column(nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType projectType;

    @Column(nullable = false)
    private String domain;

    private Instant completionDate;

    private Integer rubricScore;

    @Column(columnDefinition = "TEXT")
    private String mentorFeedback;

    @Column(columnDefinition = "TEXT")
    private String technologiesUsed;

    private String repositoryUrl;

    private String liveUrl;

    private Boolean completed;

    private String externalProjectId;
}
