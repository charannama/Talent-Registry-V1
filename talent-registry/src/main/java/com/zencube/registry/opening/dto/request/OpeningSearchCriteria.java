package com.zencube.registry.opening.dto.request;

import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.WorkMode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpeningSearchCriteria {

    @Parameter(description = "Domain to filter by (e.g. software_engineering)")
    private String domain;

    @Parameter(description = "Job Type (e.g. FULL_TIME)")
    private JobType jobType;

    @Parameter(description = "Partial, case-insensitive match on company name")
    private String company;

    @Parameter(description = "Minimum expected salary (used for overlap check against opening's salary range)")
    private BigDecimal salaryMin;

    @Parameter(description = "Maximum expected salary (used for overlap check against opening's salary range)")
    private BigDecimal salaryMax;

    @Parameter(description = "Work Mode (e.g. REMOTE)")
    private WorkMode workMode;

    @Parameter(description = "Graduation year filter")
    private String graduationYear;

    @Parameter(description = "Filter by featured openings")
    private Boolean featured;

    @Parameter(description = "If true, filters results strictly to openings the student is eligible to apply for")
    private Boolean eligibleOnly;
}
