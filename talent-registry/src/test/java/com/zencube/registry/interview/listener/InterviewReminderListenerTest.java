package com.zencube.registry.interview.listener;

import com.zencube.registry.interview.event.InterviewCancelledEvent;
import com.zencube.registry.interview.event.InterviewRescheduledEvent;
import com.zencube.registry.interview.event.InterviewScheduledEvent;
import com.zencube.registry.interview.service.InterviewReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InterviewReminderListenerTest {

    @Mock
    private InterviewReminderService interviewReminderService;

    @InjectMocks
    private InterviewReminderListener listener;

    @Test
    void shouldHandleInterviewScheduledEvent() {
        InterviewScheduledEvent event = InterviewScheduledEvent.builder()
                .interviewId(UUID.randomUUID())
                .interviewTime(Instant.now())
                .build();

        listener.onInterviewScheduled(event);

        verify(interviewReminderService).scheduleReminders(event);
    }

    @Test
    void shouldHandleInterviewRescheduledEvent() {
        InterviewRescheduledEvent event = InterviewRescheduledEvent.builder()
                .interviewId(UUID.randomUUID())
                .newInterviewTime(Instant.now())
                .build();

        listener.onInterviewRescheduled(event);

        verify(interviewReminderService).rescheduleReminders(event);
    }

    @Test
    void shouldHandleInterviewCancelledEvent() {
        InterviewCancelledEvent event = InterviewCancelledEvent.builder()
                .interviewId(UUID.randomUUID())
                .build();

        listener.onInterviewCancelled(event);

        verify(interviewReminderService).cancelReminders(event.getInterviewId());
    }
}
