package com.zencube.registry.profile.mapper;

import com.zencube.registry.profile.dto.response.ProfileResponse;
import com.zencube.registry.profile.entity.StudentProfile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public static ProfileResponse toResponse(StudentProfile profile) {
        if (profile == null) {
            return null;
        }

        return ProfileResponse.builder()
                .profileId(profile.getId())
                .name(profile.getUser() != null ? profile.getUser().getDisplayName() : null)
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .avatarUrl(profile.getAvatarUrl())
                .institution(profile.getInstitution())
                .discipline(profile.getDiscipline())
                .graduationYear(profile.getGraduationYear())
                .gpa(profile.getGpa())
                .location(profile.getLocation())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .portfolioUrl(profile.getPortfolioUrl())
                .eligibilityLevel(profile.getEligibilityLevel())
                .lastSyncAt(profile.getLastSyncAt())
                .build();
    }
}
