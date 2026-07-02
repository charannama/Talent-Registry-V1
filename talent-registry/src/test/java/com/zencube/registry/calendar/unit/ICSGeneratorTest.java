package com.zencube.registry.calendar.unit;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.enums.ParticipantType;
import com.zencube.registry.calendar.export.ICSConstants;
import com.zencube.registry.calendar.export.ICSGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ICSGeneratorTest {

    private ICSGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ICSGenerator();
    }

    @Test
    void testGenerateICS_SingleEvent() {
        CalendarEvent event = new CalendarEvent();
        event.setId(UUID.randomUUID());
        event.setTitle("Test Event");
        event.setDescription("This is a test description that is intentionally long to test the folding logic of the RFC 5545 specification. It must fold perfectly over 75 characters.");
        event.setStartTime(Instant.parse("2026-10-01T10:00:00Z"));
        event.setEndTime(Instant.parse("2026-10-01T11:00:00Z"));
        event.setLocation("Virtual Room");

        CalendarParticipant guest = new CalendarParticipant();
        guest.setParticipantType(ParticipantType.EXTERNAL);
        guest.setExternalEmail("guest@example.com");

        String ics = generator.generateICS(event, List.of(guest));

        // Core RFC tags
        assertTrue(ics.contains("BEGIN:VCALENDAR"));
        assertTrue(ics.contains("VERSION:2.0"));
        assertTrue(ics.contains("BEGIN:VEVENT"));
        assertTrue(ics.contains("SUMMARY:Test Event"));
        assertTrue(ics.contains("DTSTART:20261001T100000Z"));
        assertTrue(ics.contains("DTEND:20261001T110000Z"));
        assertTrue(ics.contains("LOCATION:Virtual Room"));
        assertTrue(ics.contains("ATTENDEE;RSVP=TRUE;CN=Guest:mailto:guest@example.com"));

        // Test folding
        assertTrue(ics.contains(ICSConstants.CRLF + " "));
    }
}
