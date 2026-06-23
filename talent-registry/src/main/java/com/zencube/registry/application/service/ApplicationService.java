package com.zencube.registry.application.service;

import com.zencube.registry.application.dto.response.ApplicationPageResponse;
import com.zencube.registry.application.dto.response.PendingApplicationResponse;

import com.zencube.registry.application.dto.response.EnterpriseApplicationPageResponse;
import com.zencube.registry.application.dto.response.EnterpriseApplicationResponse;
import com.zencube.registry.application.entity.Application;

import java.util.UUID;

public interface ApplicationService {
    ApplicationPageResponse<PendingApplicationResponse> getPendingReviewQueue(String status, String search, int page, int size, String sort, String direction);
    EnterpriseApplicationPageResponse<EnterpriseApplicationResponse> getForwardedApplicationsForEnterprise(UUID openingId, String search, String status, int page, int size, String sort, String direction, UUID currentUserId);
    Application applyToOpening(UUID openingId, UUID currentUserId);
}
