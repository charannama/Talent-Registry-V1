package com.zencube.registry.notification.repository;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.common.TestDataFactory;
import com.zencube.registry.notification.entity.NotificationSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSettingsRepositoryTest extends IntegrationTestBase {

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void findByUserId_ShouldReturnSettings() {
        // Arrange
        User user = testDataFactory.createUser("settings.test@example.com");

        NotificationSettings settings = NotificationSettings.builder()
                .userId(user.getId())
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();
                
        notificationSettingsRepository.save(settings);

        // Act
        Optional<NotificationSettings> found = notificationSettingsRepository.findByUserId(user.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(user.getId());
        assertThat(found.get().getEmailEnabled()).isTrue();
        assertThat(found.get().getPushEnabled()).isFalse();
    }

    @Test
    void existsByUserId_ShouldReturnTrueIfSettingsExist() {
        // Arrange
        User user = testDataFactory.createUser("settings.exist.test@example.com");

        NotificationSettings settings = NotificationSettings.builder()
                .userId(user.getId())
                .emailEnabled(true)
                .pushEnabled(false)
                .inAppEnabled(true)
                .build();
                
        notificationSettingsRepository.save(settings);

        // Act
        boolean exists = notificationSettingsRepository.existsByUserId(user.getId());

        // Assert
        assertThat(exists).isTrue();
    }
}
