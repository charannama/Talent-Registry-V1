package com.zencube.registry.calendar.integration;

import com.zencube.registry.calendar.dto.request.CreateCalendarEventRequest;
import com.zencube.registry.calendar.dto.request.UpdateCalendarEventRequest;
import com.zencube.registry.calendar.dto.response.CalendarEventResponse;
import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.enums.EventCategory;
import com.zencube.registry.calendar.enums.ParticipantType;
import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import com.zencube.registry.calendar.repository.CalendarEventRepository;
import com.zencube.registry.calendar.repository.CalendarParticipantRepository;
import com.zencube.registry.calendar.service.CalendarService;
import com.zencube.registry.common.IntegrationTestBase;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarIT extends IntegrationTestBase {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    @Autowired
    private CalendarParticipantRepository calendarParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("participant-" + UUID.randomUUID() + "@test.com");
        testUser.setPasswordHash("password");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCalendarModule_CRUD_and_ParticipantManagement() {
        // 1. Create Event
        CreateCalendarEventRequest createRequest = CreateCalendarEventRequest.builder()
                .title("Integration Test Event")
                .description("Testing end-to-end event flow")
                .startTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .timezone("UTC")
                .location("Virtual")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("CUSTOM_INTEGRATION")
                .eventableId(UUID.randomUUID())
                .build();

        CalendarEventResponse createdEvent = calendarService.createEvent(createRequest);
        assertNotNull(createdEvent.getId());
        assertEquals("Integration Test Event", createdEvent.getTitle());

        // 2. Fetch Event
        CalendarEventResponse fetchedEvent = calendarService.getEvent(createdEvent.getId());
        assertNotNull(fetchedEvent);
        assertEquals(createdEvent.getId(), fetchedEvent.getId());

        // 3. Add Participant manually (Simulating participant management)
        CalendarEvent eventEntity = calendarEventRepository.findById(createdEvent.getId()).orElseThrow();
        CalendarParticipant participant = CalendarParticipant.builder()
                .event(eventEntity)
                .user(testUser)
                .participantType(ParticipantType.INTERNAL)
                .responseStatus(ParticipantResponseStatus.PENDING)
                .build();
        participant = calendarParticipantRepository.save(participant);

        // Verify participant count via service
        CalendarEventResponse eventWithParticipant = calendarService.getEvent(createdEvent.getId());
        // Depending on mapper, participantCount might be there, we can check repository
        assertEquals(1L, calendarParticipantRepository.countByEventId(createdEvent.getId()));

        // 4. Update Event
        UpdateCalendarEventRequest updateRequest = UpdateCalendarEventRequest.builder()
                .title("Integration Test Event - Updated")
                .description("Updated description")
                .startTime(createRequest.getStartTime())
                .endTime(createRequest.getEndTime())
                .timezone("UTC")
                .location("Updated Location")
                .eventCategory(EventCategory.INTERVIEW)
                .build();

        CalendarEventResponse updatedEvent = calendarService.updateEvent(createdEvent.getId(), updateRequest);
        assertEquals("Integration Test Event - Updated", updatedEvent.getTitle());

        // 5. Delete Participant
        calendarParticipantRepository.delete(participant);
        assertEquals(0L, calendarParticipantRepository.countByEventId(createdEvent.getId()));

        // 6. Delete Event
        calendarService.deleteEvent(createdEvent.getId());
        assertTrue(calendarEventRepository.findById(createdEvent.getId()).isEmpty());
    }

    @Test
    void testCalendarModule_ConflictDetection() {
        UUID sharedEntityId = UUID.randomUUID();
        Instant startTime = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant endTime = startTime.plus(1, ChronoUnit.HOURS);

        CreateCalendarEventRequest firstEvent = CreateCalendarEventRequest.builder()
                .title("First Event")
                .startTime(startTime)
                .endTime(endTime)
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("CONFLICT_TEST")
                .eventableId(sharedEntityId)
                .build();

        calendarService.createEvent(firstEvent);

        // Attempting to create an overlapping event for the same entity should fail/conflict
        CreateCalendarEventRequest conflictingEvent = CreateCalendarEventRequest.builder()
                .title("Conflicting Event")
                .startTime(startTime.plus(30, ChronoUnit.MINUTES))
                .endTime(endTime.plus(30, ChronoUnit.MINUTES))
                .timezone("UTC")
                .eventCategory(EventCategory.INTERVIEW)
                .eventableType("CONFLICT_TEST")
                .eventableId(sharedEntityId)
                .build();

        assertTrue(calendarService.checkConflicts("CONFLICT_TEST", sharedEntityId, conflictingEvent.getStartTime(), conflictingEvent.getEndTime(), null));
    }
}



