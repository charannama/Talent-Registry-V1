package com.zencube.registry.notification.fixtures;

import com.zencube.registry.notification.entity.Notification;
import com.zencube.registry.notification.entity.NotificationSettings;
import com.zencube.registry.scheduler.entity.ScheduledTask;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationAssertions {

    public static void assertNotificationCreated(Notification notification, String expectedTitle) {
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertEquals(expectedTitle, notification.getTitle());
        assertFalse((notification.getReadAt() != null));
    }

    public static void assertNotificationRead(Notification notification) {
        assertNotNull(notification);
        assertTrue((notification.getReadAt() != null));
        assertNotNull(notification.getReadAt());
    }

    public static void assertTaskQueued(ScheduledTask task, String expectedType) {
        assertNotNull(task);
        assertNotNull(task.getId());
        assertEquals(expectedType, task.getTaskType());
        assertEquals("PENDING", task.getState().name());
    }

    public static void assertPreferenceApplied(NotificationSettings settings, boolean expectedEmail, boolean expectedPush) {
        assertNotNull(settings);
        assertEquals(expectedEmail, settings.getEmailEnabled());
        assertEquals(expectedPush, settings.getPushEnabled());
    }
}

