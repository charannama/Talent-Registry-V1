package com.zencube.registry.opening.dto.request;

import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body to create a job opening in Draft mode")
public class CreateOpeningRequest {

    @NotNull(message = "Enterprise ID is required")
    @Schema(description = "UUID of the enterprise account owning the opening", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID enterpriseId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Job title", example = "Senior Software Engineer")
    private String title;

    @Schema(description = "Detailed job description", example = "We are looking for a Senior Spring Boot developer...")
    private String description;

    @Schema(description = "Job requirements and qualifications", example = "5+ years of Java experience, knowledge of PostgreSQL...")
    private String requirements;

    @Schema(description = "Job location", example = "New York, NY")
    private String location;

    @Schema(description = "Employment type (FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT)", example = "FULL_TIME")
    private JobType jobType;

    @Schema(description = "Functional domain", example = "Engineering")
    private String domain;

    @DecimalMin(value = "0.0", message = "Minimum salary must be greater than or equal to 0")
    @Schema(description = "Minimum salary offered", example = "80000.00")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Maximum salary must be greater than or equal to 0")
    @Schema(description = "Maximum salary offered", example = "120000.00")
    private BigDecimal salaryMax;

    @Schema(description = "Work mode configuration (REMOTE, HYBRID, ONSITE)", example = "HYBRID")
    private WorkMode workMode;

    @Min(value = 1, message = "Positions count must be at least 1")
    @Schema(description = "Number of open positions available", example = "2")
    private Integer positions;

    @Future(message = "Application deadline must be in the future")
    @Schema(description = "UTC ISO-8601 timestamp for application deadline", example = "2026-12-31T23:59:59Z")
    private Instant deadline;

    @Schema(description = "List of required skills", example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\"]")
    private List<String> requiredSkills;

    @Schema(description = "Target graduation years", example = "[\"2024\", \"2025\"]")
    private List<String> graduationYears;
}
