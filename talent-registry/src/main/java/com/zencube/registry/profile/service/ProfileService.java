package com.zencube.registry.profile.service;

import com.zencube.registry.profile.dto.response.ProfileResponse;

public interface ProfileService {
    ProfileResponse getMyProfile();

    ProfileResponse getProfileByUserId(java.util.UUID targetUserId, jakarta.servlet.http.HttpServletRequest request);
}
