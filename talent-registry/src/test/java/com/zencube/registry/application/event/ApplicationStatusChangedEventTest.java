package com.zencube.registry.application.event;

import com.zencube.registry.common.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationStatusChangedEventTest {

    @Test
    void testEventCreation() {
        UUID appId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID enterpriseId = UUID.randomUUID();
        UUID openingId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();

        ApplicationStatusChangedEvent event = ApplicationStatusChangedEvent.builder()
                .applicationId(appId)
                .studentId(studentId)
                .enterpriseId(enterpriseId)
                .openingId(openingId)
                .oldStatus(ApplicationStatus.APPLIED)
                .newStatus(ApplicationStatus.UNDER_REVIEW)
                .actorId(actorId)
                .actorType("HR")
                .occurredAt(now)
                .build();

        assertNotNull(event);
        assertEquals(appId, event.getApplicationId());
        assertEquals(studentId, event.getStudentId());
        assertEquals(enterpriseId, event.getEnterpriseId());
        assertEquals(openingId, event.getOpeningId());
        assertEquals(ApplicationStatus.APPLIED, event.getOldStatus());
        assertEquals(ApplicationStatus.UNDER_REVIEW, event.getNewStatus());
        assertEquals(actorId, event.getActorId());
        assertEquals("HR", event.getActorType());
        assertEquals(now, event.getOccurredAt());
    }
}
