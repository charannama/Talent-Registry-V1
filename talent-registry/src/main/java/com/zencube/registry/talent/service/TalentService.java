package com.zencube.registry.talent.service;

import com.zencube.registry.talent.dto.request.TalentSearchRequest;
import com.zencube.registry.talent.dto.response.TalentProfileResponse;
import com.zencube.registry.talent.dto.response.TalentSearchResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TalentService {

    TalentSearchResponse searchTalent(TalentSearchRequest request, Pageable pageable);

    TalentProfileResponse getProfile(UUID profileId, HttpServletRequest request);

    void suspendProfile(UUID profileId, String reason, String suspendedBy);

    void reinstateProfile(UUID profileId);
}
