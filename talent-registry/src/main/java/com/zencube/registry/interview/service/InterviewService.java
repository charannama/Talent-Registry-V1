package com.zencube.registry.interview.service;

import java.time.Instant;
import java.util.UUID;

public interface InterviewService {

    void scheduleInterview(UUID applicationId, UUID studentId, UUID enterpriseId, UUID openingId, String title, Instant interviewTime, String timezone, UUID scheduledBy);

    void rescheduleInterview(UUID interviewId, Instant newTime, String newTimezone, UUID rescheduledBy);

    void cancelInterview(UUID interviewId, UUID cancelledBy);
}
