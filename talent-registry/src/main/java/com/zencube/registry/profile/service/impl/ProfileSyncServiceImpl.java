package com.zencube.registry.profile.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.profile.dto.response.ProfileResponse;
import com.zencube.registry.profile.entity.ProfileSyncAudit;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.entity.StudentProject;
import com.zencube.registry.profile.enums.EligibilityLevel;
import com.zencube.registry.profile.enums.ProjectType;
import com.zencube.registry.profile.enums.SyncStatus;
import com.zencube.registry.profile.mapper.ProfileMapper;
import com.zencube.registry.profile.repository.ProfileSyncAuditRepository;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import com.zencube.registry.profile.repository.StudentProjectRepository;
import com.zencube.registry.profile.service.ProfileSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileSyncServiceImpl implements ProfileSyncService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final StudentProjectRepository projectRepository;
    private final ProfileSyncAuditRepository auditRepository;

    @Override
    @Transactional
    public ProfileResponse syncProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    StudentProfile newProfile = new StudentProfile();
                    newProfile.setUser(user);
                    newProfile.setSyncStatus(SyncStatus.NEVER_SYNCED);
                    newProfile.setEligibilityLevel(EligibilityLevel.NO_PROJECT);
                    return profileRepository.save(newProfile);
                });

        ProfileSyncAudit audit = new ProfileSyncAudit();
        audit.setProfile(profile);
        audit.setSyncStartTime(Instant.now());

        try {
            profile.setSyncStatus(SyncStatus.IN_PROGRESS);
            profile = profileRepository.save(profile);

            // Mock Fetching ZenCube Data
            // Mock Update Academic Info
            // Mock Update Skills
            syncProjects(profile);
            syncExperiences(profile);

            profile.setEligibilityLevel(calculateEligibility(profile));
            profile.setLastSyncAt(Instant.now());
            profile.setSyncStatus(SyncStatus.SUCCESS);
            profile.setSyncError(null);

            audit.setStatus(SyncStatus.SUCCESS);

        } catch (Exception e) {
            log.error("Failed to sync profile for user: {}", user.getEmail(), e);
            profile.setSyncStatus(SyncStatus.FAILED);
            profile.setSyncError(e.getMessage());
            
            audit.setStatus(SyncStatus.FAILED);
            audit.setErrorMessage(e.getMessage());
        }

        profile = profileRepository.save(profile);
        
        audit.setSyncEndTime(Instant.now());
        auditRepository.save(audit);
        
        return ProfileMapper.toResponse(profile);
    }

    private void syncProjects(StudentProfile profile) {
        // Mock ZenCube sync flow
        log.info("Syncing projects from ZenCube for profile: {}", profile.getId());
        // Fetch ZenCube Projects -> Find Existing -> Update -> Insert Missing -> Ignore Duplicates
    }

    private void syncExperiences(StudentProfile profile) {
        // Mock ZenCube sync flow
        log.info("Syncing work experiences from ZenCube for profile: {}", profile.getId());
        // Fetch ZenCube Experiences -> Find Existing -> Update -> Insert Missing -> Ignore Duplicates
    }

    private EligibilityLevel calculateEligibility(StudentProfile profile) {
        List<StudentProject> projects = projectRepository.findByProfileId(profile.getId());

        boolean hasCapstone = projects.stream().anyMatch(p -> p.getProjectType() == ProjectType.CAPSTONE);
        boolean hasMini = projects.stream().anyMatch(p -> p.getProjectType() == ProjectType.MINI);
        boolean hasNano = projects.stream().anyMatch(p -> p.getProjectType() == ProjectType.NANO);

        if (hasCapstone) return EligibilityLevel.CAPSTONE;
        if (hasMini) return EligibilityLevel.MINI;
        if (hasNano) return EligibilityLevel.NANO;

        return EligibilityLevel.NO_PROJECT;
    }
}
