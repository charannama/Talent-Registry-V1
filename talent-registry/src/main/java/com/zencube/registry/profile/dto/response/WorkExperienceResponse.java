package com.zencube.registry.profile.dto.response;

import com.zencube.registry.profile.enums.EmploymentType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceResponse {

    private UUID id;

    private String jobTitle;

    private String companyName;

    private String location;

    private EmploymentType employmentType;

    private Instant startDate;

    private Instant endDate;

    private Boolean currentlyWorking;

    private String description;

    private String keyResponsibilities;
}
