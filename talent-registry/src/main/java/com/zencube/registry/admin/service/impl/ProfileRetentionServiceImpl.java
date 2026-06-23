package com.zencube.registry.admin.service.impl;

import com.zencube.registry.admin.dto.response.RetentionStatusResponse;
import com.zencube.registry.admin.entity.ProfileRetentionAudit;
import com.zencube.registry.admin.enums.FreezeReason;
import com.zencube.registry.admin.repository.ProfileRetentionAuditRepository;
import com.zencube.registry.admin.service.ProfileRetentionService;
import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.common.exception.ConflictException;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileRetentionServiceImpl implements ProfileRetentionService {

    private final StudentProfileRepository profileRepository;
    private final ApplicationRepository applicationRepository;
    private final ProfileRetentionAuditRepository auditRepository;

    private static final List<ApplicationStatus> ACTIVE_STATUSES = List.of(
            ApplicationStatus.SUBMITTED,
            ApplicationStatus.UNDER_REVIEW,
            ApplicationStatus.SHORTLISTED,
            ApplicationStatus.ASSESSMENT_SENT,
            ApplicationStatus.ASSESSMENT_COMPLETED,
            ApplicationStatus.INTERVIEW_SCHEDULED,
            ApplicationStatus.INTERVIEW_COMPLETED,
            ApplicationStatus.OFFER_EXTENDED,
            ApplicationStatus.OFFER_ACCEPTED
    );

    @Override
    @Transactional
    public RetentionStatusResponse checkRetention(UUID profileId) {
        StudentProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        RetentionStatusResponse response = calculateRetentionStatus(profile);
        saveAuditRecord(profile, response);
        return response;
    }

    @Override
    @Transactional
    public void deleteProfile(UUID profileId, String adminEmail) {
        StudentProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        RetentionStatusResponse status = calculateRetentionStatus(profile);

        if (!status.isCanDelete()) {
            throw new ConflictException("Profile cannot be deleted. Reason: " + status.getFreezeReason());
        }

        profile.setDeleted(true);
        profile.setDeletedAt(Instant.now());
        profile.setDeletedBy(adminEmail);
        profileRepository.save(profile);

        // Also logically delete the User entity?
        // Let's assume the user handles that or we do it if requested, but RFC says "Delete Profile"
    }

    private RetentionStatusResponse calculateRetentionStatus(StudentProfile profile) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate gradDate = profile.getGraduationDate();

        // 1. Graduation Rule
        if (gradDate == null || gradDate.isAfter(today)) {
            return buildResponse(false, FreezeReason.NOT_GRADUATED, null, gradDate, profile.getId());
        }

        // 2. Retention Window Rule (+1 Year)
        LocalDate expiryDate = gradDate.plusYears(1);
        Instant expiryInstant = expiryDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        if (expiryDate.isAfter(today)) {
            long daysRemaining = ChronoUnit.DAYS.between(today, expiryDate);
            RetentionStatusResponse resp = buildResponse(false, FreezeReason.RETENTION_WINDOW, expiryInstant, gradDate, profile.getId());
            resp.setDaysRemaining(daysRemaining);
            return resp;
        }

        // 3. Active Applications Rule
        int activeCount = applicationRepository.countActiveApplications(profile.getId(), ACTIVE_STATUSES);
        List<ApplicationStatus> statuses = applicationRepository.findMostAdvancedStatus(profile.getId(), ACTIVE_STATUSES);
        ApplicationStatus mostAdvanced = statuses.isEmpty() ? null : statuses.get(0);

        if (activeCount > 0) {
            RetentionStatusResponse resp = buildResponse(false, FreezeReason.ACTIVE_APPLICATIONS, expiryInstant, gradDate, profile.getId());
            resp.setActiveApplicationCount(activeCount);
            resp.setMostAdvancedStatus(mostAdvanced);
            return resp;
        }

        // 4. Eligible for deletion
        return buildResponse(true, FreezeReason.NONE, expiryInstant, gradDate, profile.getId());
    }

    private RetentionStatusResponse buildResponse(boolean canDelete, FreezeReason reason, Instant expiresAt, LocalDate gradDate, UUID profileId) {
        return RetentionStatusResponse.builder()
                .canDelete(canDelete)
                .freezeReason(reason)
                .retentionExpiresAt(expiresAt)
                .graduationDate(gradDate)
                .build();
    }

    private void saveAuditRecord(StudentProfile profile, RetentionStatusResponse response) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        ProfileRetentionAudit audit = ProfileRetentionAudit.builder()
                .profile(profile)
                .checkedAt(Instant.now())
                .checkedBy(adminEmail)
                .canDelete(response.isCanDelete())
                .freezeReason(response.getFreezeReason())
                .retentionExpiresAt(response.getRetentionExpiresAt())
                .activeApplicationCount(response.getActiveApplicationCount())
                .mostAdvancedStatus(response.getMostAdvancedStatus())
                .build();
        
        auditRepository.save(audit);
    }
}
