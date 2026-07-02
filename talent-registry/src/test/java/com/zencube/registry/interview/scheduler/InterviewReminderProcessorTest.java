package com.zencube.registry.interview.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.config.service.ConfigService;
import com.zencube.registry.interview.dto.InterviewReminderPayload;
import com.zencube.registry.interview.dto.InterviewReminderType;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.notification.service.NotificationService;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewReminderProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TaskSchedulerService taskSchedulerService;

    @Mock
    private ConfigService configService;

    @Mock
    private AuditService auditService;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private InterviewReminderProcessor processor;

    private ScheduledTask mockTask;
    private InterviewReminderPayload mockPayload;

    @BeforeEach
    void setUp() {
        mockTask = new ScheduledTask();
        mockTask.setId(UUID.randomUUID());
        mockTask.setPayload(Collections.emptyMap());

        mockPayload = InterviewReminderPayload.builder()
                .interviewId(UUID.randomUUID())
                .studentId(UUID.randomUUID())
                .interviewTitle("Java Developer Interview")
                .interviewTime(Instant.now())
                .timezone("UTC")
                .reminderType(InterviewReminderType.REMINDER_24_HOURS)
                .build();
    }

    @Test
    void testProcess_WithNotificationsAndEmailsEnabled() {
        when(objectMapper.convertValue(any(), eq(InterviewReminderPayload.class))).thenReturn(mockPayload);
        when(configService.get(anyString(), eq(Boolean.class))).thenReturn(true);

        processor.process(mockTask);

        verify(notificationService).createNotification(any(), any(), anyString(), any(), anyString(), anyString());
        verify(taskSchedulerService).enqueueEmailTask(any());
        verify(auditService).recordCustomEvent(eq("INTERVIEW_REMINDER_SENT"), anyString(), anyString(), anyString());
        verify(activityService).recordActivity(anyString(), anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void testProcess_WithDisabledConfig() {
        when(objectMapper.convertValue(any(), eq(InterviewReminderPayload.class))).thenReturn(mockPayload);
        // Disable 24h reminders globally
        when(configService.get(eq("INTERVIEW.REMINDER_24H_ENABLED"), eq(Boolean.class))).thenReturn(false);

        processor.process(mockTask);

        // It should return early, no notifications or emails
        verify(notificationService, never()).createNotification(any(), any(), anyString(), any(), anyString(), anyString());
        verify(taskSchedulerService, never()).enqueueEmailTask(any());
    }
}
