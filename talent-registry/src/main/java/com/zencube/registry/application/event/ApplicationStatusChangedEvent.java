package com.zencube.registry.application.event;

import com.zencube.registry.common.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event representing a state transition for an Application.
 * Published whenever an application's status changes.
 */
@Getter
@Builder
public final class ApplicationStatusChangedEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final UUID applicationId;
    private final UUID studentId;
    private final UUID enterpriseId;
    private final UUID openingId;
    private final ApplicationStatus oldStatus;
    private final ApplicationStatus newStatus;
    private final UUID actorId;
    private final String actorType;
    private final Instant occurredAt;
    
    // Future expansion for Kafka compatibility could include topic names or schemas here
}
