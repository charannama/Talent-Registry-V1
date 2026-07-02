package com.zencube.registry.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewReminderPayload {
    private UUID interviewId;
    private UUID applicationId;
    private UUID studentId;
    private UUID enterpriseId;
    private UUID openingId;
    private String interviewTitle;
    private Instant interviewTime;
    private String timezone;
    private InterviewReminderType reminderType;
}
