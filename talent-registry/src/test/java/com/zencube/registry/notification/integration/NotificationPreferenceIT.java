package com.zencube.registry.notification.integration;

import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.notification.repository.NotificationSettingsRepository;
import com.zencube.registry.notification.service.NotificationPreferenceService;
import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;
import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationPreferenceIT extends IntegrationTestBase {

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private NotificationSettingsRepository settingsRepository;

    @Test
    @Transactional
    void getSettings_createsDefaultAndPersists() {
        UUID userId = UUID.randomUUID();
        
        NotificationSettingsResponse response = preferenceService.getSettings(userId);
        
        assertTrue(response.getEmailEnabled());
        assertTrue(settingsRepository.findByUserId(userId).isPresent());
    }

    @Test
    @Transactional
    void updateSettings_persistsChanges() {
        UUID userId = UUID.randomUUID();
        preferenceService.getSettings(userId); // Ensure it exists
        
        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setEmailEnabled(false);
        request.setPushEnabled(true);
        request.setInAppEnabled(true);
        request.setEmailEnabled(true); request.setInAppEnabled(true); request.setPushEnabled(true);;

        preferenceService.updateSettings(userId, request);

        NotificationSettingsResponse updated = preferenceService.getSettings(userId);
        assertFalse(updated.getEmailEnabled());
    }
}


