package com.zencube.registry.interview.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public final class InterviewScheduledEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID interviewId;
    private final UUID applicationId;
    private final UUID studentId;
    private final UUID enterpriseId;
    private final UUID openingId;
    private final String interviewTitle;
    private final Instant interviewTime;
    private final String timezone;
    private final UUID scheduledBy;
    private final Instant occurredAt;
}
