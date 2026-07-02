package com.zencube.registry.opening.mapper;

import com.zencube.registry.enterprise.entity.EnterpriseAccount;
import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.dto.request.CreateOpeningRequest;
import com.zencube.registry.opening.dto.response.OpeningResponse;
import com.zencube.registry.opening.enums.OpeningStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OpeningMapper {

    private OpeningMapper() {}

    public static Opening toEntity(CreateOpeningRequest request, EnterpriseAccount enterprise) {
        if (request == null) return null;

        String skillsStr = request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()
                ? String.join(",", request.getRequiredSkills())
                : null;
        String gradYearsStr = request.getGraduationYears() != null && !request.getGraduationYears().isEmpty()
                ? String.join(",", request.getGraduationYears())
                : null;

        Opening opening = new Opening();
        opening.setEnterprise(enterprise);
        opening.setTitle(request.getTitle());
        opening.setDescription(request.getDescription());
        opening.setRequirements(request.getRequirements());
        opening.setLocation(request.getLocation());
        opening.setJobType(request.getJobType());
        opening.setDomain(request.getDomain());
        opening.setSalaryMin(request.getSalaryMin());
        opening.setSalaryMax(request.getSalaryMax());
        opening.setWorkMode(request.getWorkMode());
        opening.setPositions(request.getPositions());
        opening.setApplicationDeadline(request.getDeadline());
        opening.setStatus(OpeningStatus.DRAFT);
        opening.setRequiredSkills(skillsStr);
        opening.setGraduationYears(gradYearsStr);
        opening.setGraduationYearFilter(request.getGraduationYearFilter());
        opening.setSalaryRangeMin(request.getSalaryRangeMin());
        opening.setSalaryRangeMax(request.getSalaryRangeMax());
        opening.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        
        return opening;
    }

    public static OpeningResponse toResponse(Opening opening) {
        if (opening == null) return null;

        List<String> requiredSkills = Collections.emptyList();
        if (opening.getRequiredSkills() != null && !opening.getRequiredSkills().isBlank()) {
            requiredSkills = Arrays.asList(opening.getRequiredSkills().split(","));
        }

        List<String> graduationYears = Collections.emptyList();
        if (opening.getGraduationYears() != null && !opening.getGraduationYears().isBlank()) {
            graduationYears = Arrays.asList(opening.getGraduationYears().split(","));
        }

        return OpeningResponse.builder()
                .id(opening.getId())
                .enterpriseId(opening.getEnterprise() != null ? opening.getEnterprise().getId() : null)
                .title(opening.getTitle())
                .description(opening.getDescription())
                .requirements(opening.getRequirements())
                .location(opening.getLocation())
                .jobType(opening.getJobType())
                .domain(opening.getDomain())
                .salaryMin(opening.getSalaryMin())
                .salaryMax(opening.getSalaryMax())
                .workMode(opening.getWorkMode())
                .positions(opening.getPositions())
                .deadline(opening.getApplicationDeadline())
                .status(opening.getStatus())
                .requiredSkills(requiredSkills)
                .graduationYears(graduationYears)
                .graduationYearFilter(opening.getGraduationYearFilter())
                .salaryRangeMin(opening.getSalaryRangeMin())
                .salaryRangeMax(opening.getSalaryRangeMax())
                .publishedAt(opening.getPublishedAt())
                .approvedBy(opening.getApprovedBy())
                .approvedAt(opening.getApprovedAt())
                .rejectedBy(opening.getRejectedBy())
                .rejectedAt(opening.getRejectedAt())
                .rejectionReason(opening.getRejectionReason())
                .revisionRequestedBy(opening.getRevisionRequestedBy())
                .revisionRequestedAt(opening.getRevisionRequestedAt())
                .revisionFeedback(opening.getRevisionFeedback())
                .revisionCount(opening.getRevisionCount())
                .lastResubmittedAt(opening.getLastResubmittedAt())
                .lastResubmittedBy(opening.getLastResubmittedBy())
                .closedBy(opening.getClosedBy())
                .closedAt(opening.getClosedAt())
                .closureReason(opening.getClosureReason())
                .featured(opening.getFeatured())
                .canResubmit(opening.getCanResubmit())
                .createdAt(opening.getCreatedAt())
                .createdBy(opening.getCreatedBy())
                .updatedAt(opening.getUpdatedAt())
                .updatedBy(opening.getUpdatedBy())
                .version(opening.getVersion())
                .build();
    }

    public static com.zencube.registry.opening.dto.response.OpeningSummaryResponse toSummaryResponse(Opening opening) {
        if (opening == null) return null;

        return com.zencube.registry.opening.dto.response.OpeningSummaryResponse.builder()
                .id(opening.getId())
                .title(opening.getTitle())
                .company(opening.getEnterprise() != null ? opening.getEnterprise().getCompanyName() : null)
                .location(opening.getLocation())
                .jobType(opening.getJobType())
                .domain(opening.getDomain())
                .salaryMin(opening.getSalaryMin())
                .salaryMax(opening.getSalaryMax())
                .deadline(opening.getApplicationDeadline())
                .featured(opening.getFeatured())
                .build();
    }
}
