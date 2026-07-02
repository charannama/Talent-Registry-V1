package com.zencube.registry.interview.listener;

import com.zencube.registry.interview.event.InterviewCancelledEvent;
import com.zencube.registry.interview.event.InterviewRescheduledEvent;
import com.zencube.registry.interview.event.InterviewScheduledEvent;
import com.zencube.registry.interview.service.InterviewReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewReminderListener {

    private final InterviewReminderService interviewReminderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewScheduled(InterviewScheduledEvent event) {
        log.info("Received InterviewScheduledEvent for interview {}", event.getInterviewId());
        try {
            interviewReminderService.scheduleReminders(event);
        } catch (Exception e) {
            log.error("Failed to schedule reminders for interview {}", event.getInterviewId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewRescheduled(InterviewRescheduledEvent event) {
        log.info("Received InterviewRescheduledEvent for interview {}", event.getInterviewId());
        try {
            interviewReminderService.rescheduleReminders(event);
        } catch (Exception e) {
            log.error("Failed to reschedule reminders for interview {}", event.getInterviewId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewCancelled(InterviewCancelledEvent event) {
        log.info("Received InterviewCancelledEvent for interview {}", event.getInterviewId());
        try {
            interviewReminderService.cancelReminders(event.getInterviewId());
        } catch (Exception e) {
            log.error("Failed to cancel reminders for interview {}", event.getInterviewId(), e);
        }
    }
}
