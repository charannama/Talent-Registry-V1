package com.zencube.registry.admin.dto.response;

import com.zencube.registry.admin.enums.FreezeReason;
import com.zencube.registry.common.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class RetentionStatusResponse {
    
    private boolean canDelete;
    
    private FreezeReason freezeReason;
    
    private Instant retentionExpiresAt;
    
    private Integer activeApplicationCount;
    
    private ApplicationStatus mostAdvancedStatus;
    
    private LocalDate graduationDate;
    
    private Long daysRemaining;

}
