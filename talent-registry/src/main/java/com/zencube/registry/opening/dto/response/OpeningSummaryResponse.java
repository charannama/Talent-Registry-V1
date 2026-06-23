package com.zencube.registry.opening.dto.response;

import com.zencube.registry.opening.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary of a job opening for public browsing")
public class OpeningSummaryResponse {

    @Schema(description = "UUID of the opening", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID id;

    @Schema(description = "Title of the job opening", example = "Senior Software Engineer")
    private String title;

    @Schema(description = "Name of the hiring company", example = "ZenCube Tech")
    private String company;

    @Schema(description = "Location of the job", example = "San Francisco, CA")
    private String location;

    @Schema(description = "Type of job", example = "FULL_TIME")
    private JobType jobType;

    @Schema(description = "Domain of the job", example = "Engineering")
    private String domain;

    @Schema(description = "Minimum salary", example = "120000.00")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum salary", example = "180000.00")
    private BigDecimal salaryMax;

    @Schema(description = "Deadline to apply", example = "2026-12-31T23:59:59Z")
    private Instant deadline;

    @Schema(description = "Whether the opening is featured", example = "true")
    private Boolean featured;
}
