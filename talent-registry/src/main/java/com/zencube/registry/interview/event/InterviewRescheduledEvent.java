package com.zencube.registry.interview.event;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public final class InterviewRescheduledEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID interviewId;
    private final UUID applicationId;
    private final UUID studentId;
    private final UUID enterpriseId;
    private final UUID openingId;
    private final String interviewTitle;
    private final Instant oldInterviewTime;
    private final String oldTimezone;
    private final Instant newInterviewTime;
    private final String newTimezone;
    private final UUID rescheduledBy;
    private final Instant occurredAt;
}
