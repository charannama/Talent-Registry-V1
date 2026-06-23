package com.zencube.registry.application.dto.response;

import com.zencube.registry.common.enums.ApplicationStatus;
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
public class PendingApplicationResponse {
    private UUID applicationId;
    private UUID studentId;
    private String studentName;
    private UUID openingId;
    private String openingTitle;
    private Instant appliedAt;
    private ApplicationStatus status;
}
