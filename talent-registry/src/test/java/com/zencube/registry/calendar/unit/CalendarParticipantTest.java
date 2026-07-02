package com.zencube.registry.calendar.unit;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import com.zencube.registry.calendar.enums.ParticipantType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalendarParticipantTest {

    @Test
    void testAccept() {
        CalendarParticipant participant = new CalendarParticipant();
        participant.accept();
        assertEquals(ParticipantResponseStatus.ACCEPTED, participant.getResponseStatus());
    }

    @Test
    void testDecline() {
        CalendarParticipant participant = new CalendarParticipant();
        participant.decline();
        assertEquals(ParticipantResponseStatus.DECLINED, participant.getResponseStatus());
    }

    @Test
    void testTentative() {
        CalendarParticipant participant = new CalendarParticipant();
        participant.tentative();
        assertEquals(ParticipantResponseStatus.TENTATIVE, participant.getResponseStatus());
    }

    @Test
    void testResetResponse() {
        CalendarParticipant participant = new CalendarParticipant();
        participant.accept();
        participant.resetResponse();
        assertEquals(ParticipantResponseStatus.PENDING, participant.getResponseStatus());
    }

    @Test
    void testIsInternal() {
        CalendarParticipant internal = CalendarParticipant.builder()
                .participantType(ParticipantType.INTERNAL)
                .build();
        assertTrue(internal.isInternal());
        assertFalse(internal.isExternal());
    }

    @Test
    void testIsExternal() {
        CalendarParticipant external = CalendarParticipant.builder()
                .participantType(ParticipantType.EXTERNAL)
                .build();
        assertTrue(external.isExternal());
        assertFalse(external.isInternal());
    }

    @Test
    void testGetParticipantDisplayName_Internal() {
        User user = new User();
        user.setEmail("user@test.com");
        
        CalendarParticipant internal = CalendarParticipant.builder()
                .participantType(ParticipantType.INTERNAL)
                .user(user)
                .build();
                
        assertEquals("user@test.com", internal.getParticipantDisplayName());
    }

    @Test
    void testGetParticipantDisplayName_External() {
        CalendarParticipant external = CalendarParticipant.builder()
                .participantType(ParticipantType.EXTERNAL)
                .externalEmail("guest@test.com")
                .build();
                
        assertEquals("guest@test.com", external.getParticipantDisplayName());
    }
}
