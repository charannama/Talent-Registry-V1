package com.zencube.registry.profile.entity;

import com.zencube.registry.common.entity.BaseEntity;
import com.zencube.registry.profile.enums.EmploymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "work_experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkExperience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "profile_id",
            nullable = false
    )
    private StudentProfile profile;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String companyName;

    private String location;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private Instant startDate;

    private Instant endDate;

    private Boolean currentlyWorking;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String keyResponsibilities;

    private String externalExperienceId;
}
