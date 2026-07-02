package com.zencube.registry.calendar.integration;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import com.zencube.registry.calendar.enums.ParticipantType;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.repository.CalendarParticipantRepository;
import com.zencube.registry.common.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarParticipantRepositoryTest extends IntegrationTestBase {

    @Autowired
    private CalendarParticipantRepository participantRepository;

    @Autowired
    private CalendarEventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private CalendarEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = CalendarEvent.builder()
                .title("Test Interview")
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("INTERVIEW")
                .eventableId(UUID.randomUUID())
                .build();
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @Transactional
    void findByEventId_returnsParticipants() {
        CalendarParticipant externalParticipant = CalendarParticipant.builder()
                .event(testEvent)
                .participantType(ParticipantType.EXTERNAL)
                .responseStatus(ParticipantResponseStatus.PENDING)
                .externalEmail("candidate@test.com")
                .build();
        participantRepository.save(externalParticipant);

        assertEquals(1, participantRepository.findByEventId(testEvent.getId()).size());
        assertEquals(1, participantRepository.findPendingParticipants(testEvent.getId()).size());
        assertEquals(0, participantRepository.findAcceptedParticipants(testEvent.getId()).size());
    }

    @Test
    @Transactional
    void duplicateInvitationPrevention() {
        CalendarParticipant externalParticipant = CalendarParticipant.builder()
                .event(testEvent)
                .participantType(ParticipantType.EXTERNAL)
                .responseStatus(ParticipantResponseStatus.PENDING)
                .externalEmail("duplicate@test.com")
                .build();
        participantRepository.save(externalParticipant);

        assertTrue(participantRepository.existsByEventIdAndExternalEmail(testEvent.getId(), "duplicate@test.com"));
    }

    @Test
    @Transactional
    void participantCounts() {
        CalendarParticipant p1 = CalendarParticipant.builder()
                .event(testEvent)
                .participantType(ParticipantType.EXTERNAL)
                .responseStatus(ParticipantResponseStatus.ACCEPTED)
                .externalEmail("guest1@test.com")
                .build();

        CalendarParticipant p2 = CalendarParticipant.builder()
                .event(testEvent)
                .participantType(ParticipantType.EXTERNAL)
                .responseStatus(ParticipantResponseStatus.DECLINED)
                .externalEmail("guest2@test.com")
                .build();

        participantRepository.save(p1);
        participantRepository.save(p2);

        assertEquals(2, participantRepository.countByEventId(testEvent.getId()));
        assertEquals(1, participantRepository.findAcceptedParticipants(testEvent.getId()).size());
    }
}


