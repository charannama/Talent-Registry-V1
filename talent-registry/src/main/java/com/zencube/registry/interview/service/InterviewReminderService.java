package com.zencube.registry.interview.service;

import com.zencube.registry.interview.event.InterviewRescheduledEvent;
import com.zencube.registry.interview.event.InterviewScheduledEvent;

import java.util.UUID;

public interface InterviewReminderService {

    void scheduleReminders(InterviewScheduledEvent event);

    void rescheduleReminders(InterviewRescheduledEvent event);

    void cancelReminders(UUID interviewId);
}
