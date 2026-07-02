package com.zencube.registry.application.listener;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.application.event.ApplicationStatusChangedEvent;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.enums.NotificationEventType;
import com.zencube.registry.notification.event.NotificationEvent;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationEventListenerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TaskSchedulerService taskSchedulerService;

    @Mock
    private ActivityService activityService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ApplicationEventListener listener;

    private ApplicationStatusChangedEvent createEvent(ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        return ApplicationStatusChangedEvent.builder()
                .applicationId(UUID.randomUUID())
                .studentId(UUID.randomUUID())
                .enterpriseId(UUID.randomUUID())
                .openingId(UUID.randomUUID())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .actorId(UUID.randomUUID())
                .actorType("HR")
                .occurredAt(Instant.now())
                .build();
    }

    @Test
    void testUnderReviewEvent() {
        ApplicationStatusChangedEvent event = createEvent(ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW);

        listener.onApplicationStatusChanged(event);

        verify(auditService).recordCustomEvent(eq("APPLICATION_STATUS_CHANGED"), eq("Application"), eq(event.getApplicationId().toString()), anyString());
        
        // Notification & Email checks (Student only for UNDER_REVIEW)
        ArgumentCaptor<NotificationEvent> notifCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(notifCaptor.capture());
        assertEquals(event.getStudentId(), notifCaptor.getValue().getRecipientId());
        assertEquals(NotificationEventType.APPLICATION_REVIEWED, notifCaptor.getValue().getEventType());

        ArgumentCaptor<TaskPayload> taskCaptor = ArgumentCaptor.forClass(TaskPayload.class);
        verify(taskSchedulerService, times(1)).enqueueTask(taskCaptor.capture());
        assertEquals("EMAIL_DELIVERY", taskCaptor.getValue().getTaskType());

        verify(activityService, times(1)).recordActivity(
                eq("HR"), eq(event.getActorId().toString()), eq("Application"), eq(event.getApplicationId().toString()), eq(ActivityType.REVIEWED), anyString()
        );
    }

    @Test
    void testForwardedEvent() {
        ApplicationStatusChangedEvent event = createEvent(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.FORWARDED);

        listener.onApplicationStatusChanged(event);

        // Notification & Email checks (Student AND Enterprise for FORWARDED)
        verify(eventPublisher, times(2)).publishEvent(any(NotificationEvent.class));
        verify(taskSchedulerService, times(2)).enqueueTask(any(TaskPayload.class));

        verify(activityService, times(1)).recordActivity(
                eq("HR"), eq(event.getActorId().toString()), eq("Application"), eq(event.getApplicationId().toString()), eq(ActivityType.FORWARDED), anyString()
        );
    }
}
