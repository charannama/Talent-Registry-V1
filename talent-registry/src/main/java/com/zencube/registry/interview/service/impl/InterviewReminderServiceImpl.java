package com.zencube.registry.interview.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.interview.dto.InterviewReminderPayload;
import com.zencube.registry.interview.dto.InterviewReminderType;
import com.zencube.registry.interview.event.InterviewRescheduledEvent;
import com.zencube.registry.interview.event.InterviewScheduledEvent;
import com.zencube.registry.interview.service.InterviewReminderService;
import com.zencube.registry.journal.service.AuditService;
import com.zencube.registry.scheduler.dto.TaskPayload;
import com.zencube.registry.scheduler.entity.ScheduledTask;
import com.zencube.registry.scheduler.repository.ScheduledTaskRepository;
import com.zencube.registry.scheduler.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewReminderServiceImpl implements InterviewReminderService {

    private final TaskSchedulerService taskSchedulerService;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void scheduleReminders(InterviewScheduledEvent event) {
        log.info("Scheduling reminders for interview {}", event.getInterviewId());

        // 24 Hour Reminder
        Instant reminder24h = event.getInterviewTime().minus(24, ChronoUnit.HOURS);
        if (reminder24h.isAfter(Instant.now())) {
            createReminderTask(event, reminder24h, InterviewReminderType.REMINDER_24_HOURS);
        }

        // 1 Hour Reminder
        Instant reminder1h = event.getInterviewTime().minus(1, ChronoUnit.HOURS);
        if (reminder1h.isAfter(Instant.now())) {
            createReminderTask(event, reminder1h, InterviewReminderType.REMINDER_1_HOUR);
        }

        auditService.recordCustomEvent("INTERVIEW_REMINDER_CREATED", "Interview", event.getInterviewId().toString(), "Scheduled 24h and 1h reminders");
    }

    @Override
    @Transactional
    public void rescheduleReminders(InterviewRescheduledEvent event) {
        log.info("Rescheduling reminders for interview {}", event.getInterviewId());
        
        cancelRemindersInternal(event.getInterviewId());

        InterviewScheduledEvent simulatedEvent = InterviewScheduledEvent.builder()
                .interviewId(event.getInterviewId())
                .applicationId(event.getApplicationId())
                .studentId(event.getStudentId())
                .enterpriseId(event.getEnterpriseId())
                .openingId(event.getOpeningId())
                .interviewTitle(event.getInterviewTitle())
                .interviewTime(event.getNewInterviewTime())
                .timezone(event.getNewTimezone())
                .scheduledBy(event.getRescheduledBy())
                .occurredAt(event.getOccurredAt())
                .build();
                
        scheduleReminders(simulatedEvent);
        
        auditService.recordCustomEvent("INTERVIEW_REMINDER_RESCHEDULED", "Interview", event.getInterviewId().toString(), "Rescheduled reminders");
    }

    @Override
    @Transactional
    public void cancelReminders(UUID interviewId) {
        log.info("Cancelling reminders for interview {}", interviewId);
        cancelRemindersInternal(interviewId);
        auditService.recordCustomEvent("INTERVIEW_REMINDER_CANCELLED", "Interview", interviewId.toString(), "Cancelled pending reminders");
    }

    private void cancelRemindersInternal(UUID interviewId) {
        List<ScheduledTask> pendingTasks = scheduledTaskRepository.findPendingReminderTasks(interviewId.toString());
        for (ScheduledTask task : pendingTasks) {
            task.markSkipped();
            scheduledTaskRepository.save(task);
            log.debug("Skipped pending reminder task {}", task.getId());
        }
    }

    private void createReminderTask(InterviewScheduledEvent event, Instant scheduledAt, InterviewReminderType type) {
        InterviewReminderPayload payload = InterviewReminderPayload.builder()
                .interviewId(event.getInterviewId())
                .applicationId(event.getApplicationId())
                .studentId(event.getStudentId())
                .enterpriseId(event.getEnterpriseId())
                .openingId(event.getOpeningId())
                .interviewTitle(event.getInterviewTitle())
                .interviewTime(event.getInterviewTime())
                .timezone(event.getTimezone())
                .reminderType(type)
                .build();

        Map<String, Object> data = objectMapper.convertValue(payload, new TypeReference<>() {});

        TaskPayload taskPayload = TaskPayload.builder()
                .taskType("INTERVIEW_REMINDER")
                .data(data)
                .build();

        taskSchedulerService.enqueueScheduledTask(taskPayload, scheduledAt);
    }
}
