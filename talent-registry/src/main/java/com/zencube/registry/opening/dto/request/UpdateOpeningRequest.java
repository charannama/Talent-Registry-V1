package com.zencube.registry.opening.dto.request;

import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing draft job opening")
public class UpdateOpeningRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String title;

    @Schema(description = "Job description", example = "We are looking for a Senior Spring Boot developer...")
    private String description;

    @Schema(description = "Job requirements and qualifications", example = "5+ years of Java experience...")
    private String requirements;

    @Schema(description = "Job location", example = "New York, NY")
    private String location;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private JobType jobType;

    @Schema(description = "Functional domain", example = "Engineering")
    private String domain;

    @Schema(description = "Minimum salary offered", example = "80000.00")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum salary offered", example = "120000.00")
    private BigDecimal salaryMax;

    @Schema(description = "Work mode configuration", example = "HYBRID")
    private WorkMode workMode;

    @Schema(description = "Number of open positions available", example = "2")
    private Integer positions;

    @Future(message = "Application deadline must be a future date")
    @Schema(description = "UTC ISO-8601 timestamp for application deadline", example = "2026-12-31T23:59:59Z")
    private Instant deadline;

    @Schema(description = "List of required skills", example = "[\"Java\", \"Spring Boot\"]")
    private List<String> requiredSkills;

    @Schema(description = "Target graduation years", example = "[\"2024\", \"2025\"]")
    private List<String> graduationYears;
}
