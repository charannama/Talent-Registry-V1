package com.zencube.registry.eligibility.service.impl;

import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.eligibility.dto.StudentEligibilityResponse;
import com.zencube.registry.eligibility.enums.EligibilityLevel;
import com.zencube.registry.eligibility.service.EligibilityService;
import com.zencube.registry.opening.enums.JobType;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.entity.StudentProject;
import com.zencube.registry.profile.enums.ProjectType;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import com.zencube.registry.profile.repository.StudentProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentProjectRepository studentProjectRepository;
    private final ApplicationRepository applicationRepository;

    private static final int MAX_ACTIVE_APPLICATIONS = 5;
    
    private static final List<ApplicationStatus> ACTIVE_STATUSES = List.of(
            ApplicationStatus.SUBMITTED,
            ApplicationStatus.UNDER_REVIEW,
            ApplicationStatus.SHORTLISTED,
            ApplicationStatus.ASSESSMENT_SENT,
            ApplicationStatus.ASSESSMENT_COMPLETED,
            ApplicationStatus.INTERVIEW_SCHEDULED,
            ApplicationStatus.INTERVIEW_COMPLETED,
            ApplicationStatus.OFFER_EXTENDED
    );

    @Override
    @Transactional(readOnly = true)
    public StudentEligibilityResponse getStudentEligibility(UUID userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new com.zencube.registry.common.exception.ResourceNotFoundException("Student profile not found"));

        int activeApps = applicationRepository.countActiveApplications(profile.getId(), ACTIVE_STATUSES);
        
        List<StudentProject> completedProjects = studentProjectRepository.findByProfileId(profile.getId())
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                .collect(Collectors.toList());

        EligibilityLevel level = computeLevel(completedProjects);
        List<JobType> permitted = getPermittedJobTypes(level);

        return StudentEligibilityResponse.builder()
                .profileId(profile.getId())
                .eligibilityLevel(level)
                .activeApplicationsCount(activeApps)
                .maxApplicationsReached(activeApps >= MAX_ACTIVE_APPLICATIONS)
                .permittedJobTypes(permitted)
                .graduationYear(profile.getGraduationYear() != null ? profile.getGraduationYear().toString() : null)
                .build();
    }

    private EligibilityLevel computeLevel(List<StudentProject> projects) {
        if (projects.isEmpty()) {
            return EligibilityLevel.NO_PROJECT;
        }
        
        boolean hasCapstone = projects.stream().anyMatch(p -> ProjectType.CAPSTONE.equals(p.getProjectType()));
        if (hasCapstone) return EligibilityLevel.CAPSTONE;
        
        boolean hasMini = projects.stream().anyMatch(p -> ProjectType.MINI.equals(p.getProjectType()));
        if (hasMini) return EligibilityLevel.MINI;
        
        return EligibilityLevel.NANO;
    }

    private List<JobType> getPermittedJobTypes(EligibilityLevel level) {
        switch (level) {
            case CAPSTONE:
            case MINI:
                return List.of(JobType.values());
            case NANO:
                return List.of(JobType.INTERNSHIP);
            case NO_PROJECT:
            default:
                return List.of(); // Empty, cannot apply
        }
    }
}
