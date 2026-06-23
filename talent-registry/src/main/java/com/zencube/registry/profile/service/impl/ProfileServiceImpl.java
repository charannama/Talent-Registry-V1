package com.zencube.registry.profile.service.impl;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.profile.dto.response.ProfileResponse;
import com.zencube.registry.profile.entity.StudentProfile;
import com.zencube.registry.profile.mapper.ProfileMapper;
import com.zencube.registry.profile.mapper.ProjectMapper;
import com.zencube.registry.profile.mapper.SkillMapper;
import com.zencube.registry.profile.mapper.WorkExperienceMapper;
import com.zencube.registry.profile.repository.StudentProfileRepository;
import com.zencube.registry.profile.repository.StudentProjectRepository;
import com.zencube.registry.profile.repository.StudentSkillRepository;
import com.zencube.registry.profile.repository.WorkExperienceRepository;
import com.zencube.registry.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final StudentProjectRepository projectRepository;
    private final WorkExperienceRepository experienceRepository;
    private final StudentSkillRepository skillRepository;
    private final com.zencube.registry.profile.service.ProfileAuditService auditService;
    
    private final ProjectMapper projectMapper;
    private final WorkExperienceMapper experienceMapper;
    private final SkillMapper skillMapper;

    @Override
    public ProfileResponse getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        ProfileResponse response = ProfileMapper.toResponse(profile);

        response.setProjects(projectRepository.findByProfileUserId(user.getId())
                .stream().map(projectMapper::toResponse).toList());
                
        response.setWorkExperiences(experienceRepository.findByProfileUserIdOrderByStartDateDesc(user.getId())
                .stream().map(experienceMapper::toResponse).toList());
                
        response.setSkills(skillRepository.findByProfileId(profile.getId())
                .stream().map(skillMapper::toResponse).toList());

        return response;
    }

    @Override
    public ProfileResponse getProfileByUserId(java.util.UUID targetUserId, jakarta.servlet.http.HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User viewer = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Viewer not found"));

        boolean hasPermission = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("PROFILE_VIEW_ALL"));

        if (!hasPermission) {
            auditService.logDeniedAccess(viewer.getId(), targetUserId, com.zencube.registry.profile.enums.AccessReason.OTHER, request);
            throw new org.springframework.security.access.AccessDeniedException("Missing PROFILE_VIEW_ALL permission");
        }

        StudentProfile profile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> {
                    auditService.logDeniedAccess(viewer.getId(), targetUserId, com.zencube.registry.profile.enums.AccessReason.OTHER, request);
                    return new ResourceNotFoundException("Profile not found for user: " + targetUserId);
                });

        auditService.logSuccessfulAccess(viewer.getId(), targetUserId, com.zencube.registry.profile.enums.AccessReason.PROFILE_AUDIT, request);

        ProfileResponse response = ProfileMapper.toResponse(profile);

        response.setProjects(projectRepository.findByProfileUserId(targetUserId)
                .stream().map(projectMapper::toResponse).toList());

        response.setWorkExperiences(experienceRepository.findByProfileUserIdOrderByStartDateDesc(targetUserId)
                .stream().map(experienceMapper::toResponse).toList());

        response.setSkills(skillRepository.findByProfileId(profile.getId())
                .stream().map(skillMapper::toResponse).toList());

        return response;
    }
}
