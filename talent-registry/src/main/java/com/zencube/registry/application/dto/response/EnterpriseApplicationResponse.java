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
public class EnterpriseApplicationResponse {
    private UUID applicationId;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private UUID openingId;
    private String openingTitle;
    private ApplicationStatus status;
    private Instant forwardedAt;
    private Instant lastStageUpdatedAt;
    private String resumeUrl;
    private String resumeFileName;
    private UUID currentHandlerId;
}
