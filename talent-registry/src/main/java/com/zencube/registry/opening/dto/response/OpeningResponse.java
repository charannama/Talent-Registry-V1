package com.zencube.registry.opening.dto.response;

import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.opening.enums.OpeningStatus;
import com.zencube.registry.opening.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response representation of a job opening")
public class OpeningResponse {

    @Schema(description = "UUID of the opening", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID id;

    @Schema(description = "UUID of the enterprise account", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID enterpriseId;

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

    @Schema(description = "UTC ISO-8601 timestamp for application deadline", example = "2026-12-31T23:59:59Z")
    private Instant deadline;

    @Schema(description = "Lifecycle status of the opening", example = "DRAFT")
    private OpeningStatus status;

    @Schema(description = "List of required skills", example = "[\"Java\", \"Spring Boot\"]")
    private List<String> requiredSkills;

    @Schema(description = "Target graduation years", example = "[\"2024\", \"2025\"]")
    private List<String> graduationYears;

    @Schema(description = "UTC ISO-8601 timestamp when opening was created", example = "2026-06-18T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "System identity or email of the creator", example = "recruiter@zencubetech.com")
    private String createdBy;

    @Schema(description = "UTC ISO-8601 timestamp when opening was last updated", example = "2026-06-18T10:00:00Z")
    private Instant updatedAt;

    @Schema(description = "System identity or email of the last updater", example = "recruiter@zencubetech.com")
    private String updatedBy;

    @Schema(description = "Optimistic locking version", example = "0")
    private Long version;

    @Schema(description = "UTC ISO-8601 timestamp when opening was made live", example = "2026-06-18T10:00:00Z")
    private Instant publishedAt;

    @Schema(description = "UUID of the HR user who approved the opening", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID approvedBy;

    @Schema(description = "UTC ISO-8601 timestamp when opening was approved", example = "2026-06-18T10:00:00Z")
    private Instant approvedAt;

    @Schema(description = "UUID of the HR user who rejected the opening", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID rejectedBy;

    @Schema(description = "UTC ISO-8601 timestamp when opening was rejected", example = "2026-06-18T10:00:00Z")
    private Instant rejectedAt;

    @Schema(description = "Reason for rejection", example = "Salary too low")
    private String rejectionReason;

    @Schema(description = "UUID of the HR user who requested a revision", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID revisionRequestedBy;

    @Schema(description = "UTC ISO-8601 timestamp when a revision was requested", example = "2026-06-18T10:00:00Z")
    private Instant revisionRequestedAt;

    @Schema(description = "Feedback detailing the revision required", example = "Please update salary max.")
    private String revisionFeedback;

    @Schema(description = "Number of times a revision was requested", example = "1")
    private Integer revisionCount;

    @Schema(description = "UTC ISO-8601 timestamp when the opening was last resubmitted", example = "2026-06-18T10:00:00Z")
    private Instant lastResubmittedAt;

    @Schema(description = "UUID of the enterprise user who last resubmitted the opening", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID lastResubmittedBy;

    @Schema(description = "UUID of the user who closed the opening", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID closedBy;

    @Schema(description = "UTC ISO-8601 timestamp when opening was closed", example = "2026-06-18T10:00:00Z")
    private Instant closedAt;

    @Schema(description = "Reason for closing the opening", example = "Position filled successfully")
    private String closureReason;

    @Schema(description = "Whether the opening is featured", example = "false")
    private Boolean featured;

    @Schema(description = "Whether the enterprise can revise and resubmit after rejection", example = "true")
    private Boolean canResubmit;

    @Schema(description = "Target graduation year filter", example = "2024")
    private Integer graduationYearFilter;

    @Schema(description = "Minimum salary range offered", example = "80000.00")
    private BigDecimal salaryRangeMin;

    @Schema(description = "Maximum salary range offered", example = "120000.00")
    private BigDecimal salaryRangeMax;
}
