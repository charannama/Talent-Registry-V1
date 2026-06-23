package com.zencube.registry.admin.service;

import com.zencube.registry.admin.dto.response.RetentionStatusResponse;

import java.util.UUID;

public interface ProfileRetentionService {

    RetentionStatusResponse checkRetention(UUID profileId);

    void deleteProfile(UUID profileId, String adminEmail);

}
