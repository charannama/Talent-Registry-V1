package com.zencube.registry.application.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class InterviewScheduledEvent {
    private UUID applicationId;
    private UUID calendarEventId;
    private UUID studentId;
    private UUID enterpriseId;
    private Instant scheduledTime;
    private UUID scheduledBy;
}
