package com.zencube.registry.chat.dto.response;

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
public class ApplicationContextResponse {

    private UUID applicationId;
    private String candidateName;
    private UUID candidateId;
    private String openingTitle;
    private UUID openingId;
    private String enterpriseName;
    private UUID enterpriseId;
    private ApplicationStatus status;
    private Instant appliedAt;

}
