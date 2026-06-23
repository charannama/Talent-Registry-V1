package com.zencube.registry.talent.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.entity.StudentSkill;
import com.zencube.registry.profile.mapper.ProjectMapper;
import com.zencube.registry.profile.mapper.SkillMapper;
import com.zencube.registry.profile.mapper.WorkExperienceMapper;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import com.zencube.registry.profile.repository.StudentProjectRepository;
import com.zencube.registry.profile.repository.StudentSkillRepository;
import com.zencube.registry.profile.repository.WorkExperienceRepository;
import com.zencube.registry.talent.dto.request.TalentSearchRequest;
import com.zencube.registry.talent.dto.response.TalentProfileResponse;
import com.zencube.registry.talent.dto.response.TalentProfileSummaryResponse;
import com.zencube.registry.talent.dto.response.TalentSearchResponse;
import com.zencube.registry.talent.entity.TalentProfileView;
import com.zencube.registry.talent.repository.StudentProfileSpecification;
import com.zencube.registry.talent.repository.TalentProfileViewRepository;
import com.zencube.registry.talent.service.TalentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TalentServiceImpl implements TalentService {

    private final StudentProfileRepository profileRepository;
    private final StudentSkillRepository skillRepository;
    private final StudentProjectRepository projectRepository;
    private final WorkExperienceRepository experienceRepository;
    private final TalentProfileViewRepository viewRepository;
    private final UserRepository userRepository;
    
    private final ProjectMapper projectMapper;
    private final WorkExperienceMapper experienceMapper;
    private final SkillMapper skillMapper;

    @Override
    @Transactional(readOnly = true)
    public TalentSearchResponse searchTalent(TalentSearchRequest request, Pageable pageable) {
        // Enforce max page size
        int size = Math.min(pageable.getPageSize(), 100);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());

        Page<StudentProfile> profiles = profileRepository.findAll(
                StudentProfileSpecification.build(request),
                cappedPageable
        );

        Page<TalentProfileSummaryResponse> dtoPage = profiles.map(profile -> TalentProfileSummaryResponse.builder()
                .profileId(profile.getId())
                .name(profile.getUser().getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .institution(profile.getInstitution())
                .discipline(profile.getDiscipline())
                .graduationYear(profile.getGraduationYear())
                .highestProjectType(profile.getHighestProjectType())
                .profileViews(profile.getProfileViews())
                .skills(skillRepository.findByProfileId(profile.getId()).stream()
                        .map(StudentSkill::getSkillName)
                        .collect(Collectors.toList()))
                .build()
        );

        return TalentSearchResponse.fromPage(dtoPage);
    }

    @Override
    @Transactional
    public TalentProfileResponse getProfile(UUID profileId, HttpServletRequest request) {
        StudentProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (!profile.getProfileVisible() || profile.getSuspended() || !profile.getSearchable()) {
            throw new ResourceNotFoundException("Profile is not available for viewing");
        }

        // Increment Views
        profile.setProfileViews(profile.getProfileViews() + 1);
        profileRepository.save(profile);

        // Record Audit View
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
        UUID enterpriseId = currentUser != null ? currentUser.getId() : UUID.randomUUID(); // Fallback if not mapped

        TalentProfileView view = TalentProfileView.builder()
                .enterpriseId(enterpriseId)
                .profile(profile)
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();
        viewRepository.save(view);

        // Map safe response
        return TalentProfileResponse.builder()
                .profileId(profile.getId())
                .name(profile.getUser().getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .institution(profile.getInstitution())
                .discipline(profile.getDiscipline())
                .graduationYear(profile.getGraduationYear())
                .gpa(profile.getGpa())
                .coursework(profile.getCoursework())
                .fullTimeReady(profile.getFullTimeReady())
                .internshipReady(profile.getInternshipReady())
                .remotePreference(profile.getRemotePreference())
                .eligibilityLevel(profile.getEligibilityLevel())
                .skills(skillRepository.findByProfileId(profile.getId()).stream()
                        .map(skillMapper::toResponse).toList())
                .projects(projectRepository.findByProfileUserId(profile.getUser().getId()).stream()
                        .map(projectMapper::toResponse).toList())
                .workExperiences(experienceRepository.findByProfileUserIdOrderByStartDateDesc(profile.getUser().getId()).stream()
                        .map(experienceMapper::toResponse).toList())
                .build();
    }

    @Override
    @Transactional
    public void suspendProfile(UUID profileId, String reason, String suspendedBy) {
        StudentProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setSuspended(true);
        profile.setSuspensionReason(reason);
        profile.setSuspendedAt(java.time.Instant.now());
        profile.setSuspendedBy(suspendedBy);
        profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void reinstateProfile(UUID profileId) {
        StudentProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setSuspended(false);
        profile.setSuspensionReason(null);
        profile.setSuspendedAt(null);
        profile.setSuspendedBy(null);
        profileRepository.save(profile);
    }
}
