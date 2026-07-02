package com.zencube.registry.notification.unit;

import com.zencube.registry.notification.dto.UpdateNotificationPreferenceRequest;
import com.zencube.registry.notification.dto.request.UpdateNotificationSettingsRequest;
import com.zencube.registry.notification.dto.response.NotificationSettingsResponse;
import com.zencube.registry.notification.entity.NotificationPreference;
import com.zencube.registry.notification.entity.NotificationSettings;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.repository.NotificationPreferenceRepository;
import com.zencube.registry.notification.repository.NotificationSettingsRepository;
import com.zencube.registry.notification.service.impl.NotificationPreferenceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationSettingsRepository settingsRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private NotificationPreferenceServiceImpl preferenceService;

    @Test
    void getSettings_returnsExistingSettings() {
        UUID userId = UUID.randomUUID();
        NotificationSettings settings = new NotificationSettings();
        settings.setUserId(userId);
        settings.setEmailEnabled(true);

        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(preferenceRepository.findByUserId(userId)).thenReturn(List.of());

        NotificationSettingsResponse response = preferenceService.getSettings(userId);

        assertTrue(response.getEmailEnabled());
    }

    @Test
    void getSettings_createsDefaultSettingsIfNoneExist() {
        UUID userId = UUID.randomUUID();
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(settingsRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        NotificationSettingsResponse response = preferenceService.getSettings(userId);

        assertTrue(response.getEmailEnabled());
        verify(settingsRepository).save(any(NotificationSettings.class));
    }

    @Test
    void updateSettings_success() {
        UUID userId = UUID.randomUUID();
        NotificationSettings settings = new NotificationSettings();
        settings.setUserId(userId);

        UpdateNotificationSettingsRequest request = new UpdateNotificationSettingsRequest();
        request.setEmailEnabled(false);
        request.setPushEnabled(true);
        request.setInAppEnabled(true);

        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        NotificationSettingsResponse response = preferenceService.updateSettings(userId, request);

        assertFalse(response.getEmailEnabled());
        verify(settingsRepository).save(settings);
    }
}





