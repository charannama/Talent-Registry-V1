package com.zencube.registry.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.interview.dto.InterviewReminderPayload;
import com.zencube.registry.interview.event.InterviewRescheduledEvent;
import com.zencube.registry.interview.event.InterviewScheduledEvent;
import com.zencube.registry.interview.service.impl.InterviewReminderServiceImpl;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.repository.ScheduledTaskRepository;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewReminderServiceTest {

    @Mock
    private TaskSchedulerService taskSchedulerService;

    @Mock
    private ScheduledTaskRepository scheduledTaskRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private InterviewReminderServiceImpl reminderService;

    @BeforeEach
    void setUp() {
        lenient().when(objectMapper.convertValue(any(InterviewReminderPayload.class), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                 .thenReturn(Collections.emptyMap());
    }

    @Test
    void testScheduleReminders() {
        Instant interviewTime = Instant.now().plus(48, ChronoUnit.HOURS);
        InterviewScheduledEvent event = InterviewScheduledEvent.builder()
                .interviewId(UUID.randomUUID())
                .interviewTime(interviewTime)
                .build();

        reminderService.scheduleReminders(event);

        ArgumentCaptor<Instant> timeCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(taskSchedulerService, times(2)).enqueueScheduledTask(any(TaskPayload.class), timeCaptor.capture());
        
        List<Instant> scheduledTimes = timeCaptor.getAllValues();
        assertEquals(2, scheduledTimes.size());
        
        verify(auditService).recordCustomEvent(eq("INTERVIEW_REMINDER_CREATED"), anyString(), anyString(), anyString());
    }

    @Test
    void testCancelReminders() {
        UUID interviewId = UUID.randomUUID();
        ScheduledTask mockTask = new ScheduledTask();
        mockTask.setId(UUID.randomUUID());
        
        when(scheduledTaskRepository.findPendingReminderTasks(interviewId.toString()))
                .thenReturn(List.of(mockTask));

        reminderService.cancelReminders(interviewId);

        verify(scheduledTaskRepository).save(mockTask);
        verify(auditService).recordCustomEvent(eq("INTERVIEW_REMINDER_CANCELLED"), anyString(), anyString(), anyString());
    }

    @Test
    void testRescheduleReminders() {
        UUID interviewId = UUID.randomUUID();
        Instant newTime = Instant.now().plus(48, ChronoUnit.HOURS);
        
        InterviewRescheduledEvent event = InterviewRescheduledEvent.builder()
                .interviewId(interviewId)
                .newInterviewTime(newTime)
                .build();

        when(scheduledTaskRepository.findPendingReminderTasks(interviewId.toString()))
                .thenReturn(Collections.emptyList());

        reminderService.rescheduleReminders(event);

        verify(scheduledTaskRepository).findPendingReminderTasks(interviewId.toString());
        verify(taskSchedulerService, times(2)).enqueueScheduledTask(any(), any());
        verify(auditService).recordCustomEvent(eq("INTERVIEW_REMINDER_RESCHEDULED"), anyString(), anyString(), anyString());
    }
}
