package com.zencube.registry.interview.service.impl;

import com.zencube.registry.interview.event.InterviewCancelledEvent;
import com.zencube.registry.interview.event.InterviewRescheduledEvent;
import com.zencube.registry.interview.event.InterviewScheduledEvent;
import com.zencube.registry.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void scheduleInterview(UUID applicationId, UUID studentId, UUID enterpriseId, UUID openingId, String title, Instant interviewTime, String timezone, UUID scheduledBy) {
        log.info("Scheduling interview for application {}", applicationId);
        
        UUID interviewId = UUID.randomUUID(); // Mocked persistence
        
        // Flow: Persist Interview (Skipped for mock)
        
        // Flow: Publish InterviewScheduledEvent
        InterviewScheduledEvent event = InterviewScheduledEvent.builder()
                .interviewId(interviewId)
                .applicationId(applicationId)
                .studentId(studentId)
                .enterpriseId(enterpriseId)
                .openingId(openingId)
                .interviewTitle(title)
                .interviewTime(interviewTime)
                .timezone(timezone)
                .scheduledBy(scheduledBy)
                .occurredAt(Instant.now())
                .build();

        eventPublisher.publishEvent(event);
        log.info("Published InterviewScheduledEvent for interview {}", interviewId);
    }

    @Override
    @Transactional
    public void rescheduleInterview(UUID interviewId, Instant newTime, String newTimezone, UUID rescheduledBy) {
        log.info("Rescheduling interview {}", interviewId);
        
        // Fetch old interview details (mocked)
        Instant oldTime = Instant.now();
        String oldTimezone = "UTC";
        
        InterviewRescheduledEvent event = InterviewRescheduledEvent.builder()
                .interviewId(interviewId)
                .applicationId(UUID.randomUUID())
                .studentId(UUID.randomUUID())
                .enterpriseId(UUID.randomUUID())
                .openingId(UUID.randomUUID())
                .interviewTitle("Mock Interview")
                .oldInterviewTime(oldTime)
                .oldTimezone(oldTimezone)
                .newInterviewTime(newTime)
                .newTimezone(newTimezone)
                .rescheduledBy(rescheduledBy)
                .occurredAt(Instant.now())
                .build();

        eventPublisher.publishEvent(event);
        log.info("Published InterviewRescheduledEvent for interview {}", interviewId);
    }

    @Override
    @Transactional
    public void cancelInterview(UUID interviewId, UUID cancelledBy) {
        log.info("Cancelling interview {}", interviewId);
        
        InterviewCancelledEvent event = InterviewCancelledEvent.builder()
                .interviewId(interviewId)
                .applicationId(UUID.randomUUID())
                .studentId(UUID.randomUUID())
                .enterpriseId(UUID.randomUUID())
                .openingId(UUID.randomUUID())
                .interviewTitle("Mock Interview")
                .cancelledBy(cancelledBy)
                .occurredAt(Instant.now())
                .build();

        eventPublisher.publishEvent(event);
        log.info("Published InterviewCancelledEvent for interview {}", interviewId);
    }
}
