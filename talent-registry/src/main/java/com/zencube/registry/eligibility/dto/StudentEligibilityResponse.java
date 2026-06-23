package com.zencube.registry.eligibility.dto;

import com.zencube.registry.eligibility.enums.EligibilityLevel;
import com.zencube.registry.opening.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEligibilityResponse {

    private EligibilityLevel eligibilityLevel;
    
    private int activeApplicationsCount;
    
    private boolean maxApplicationsReached;
    
    private List<JobType> permittedJobTypes;
    
    private String graduationYear;

    private java.util.UUID profileId;
    
}
