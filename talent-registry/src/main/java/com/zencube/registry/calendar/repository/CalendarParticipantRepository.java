package com.zencube.registry.calendar.repository;

import com.zencube.registry.calendar.entity.CalendarParticipant;
import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CalendarParticipantRepository extends JpaRepository<CalendarParticipant, UUID>, JpaSpecificationExecutor<CalendarParticipant> {

    List<CalendarParticipant> findByEventId(UUID eventId);

    List<CalendarParticipant> findByUserId(UUID userId);

    List<CalendarParticipant> findByExternalEmail(String externalEmail);

    List<CalendarParticipant> findByEventIdAndResponseStatus(UUID eventId, ParticipantResponseStatus status);

    default List<CalendarParticipant> findAcceptedParticipants(UUID eventId) {
        return findByEventIdAndResponseStatus(eventId, ParticipantResponseStatus.ACCEPTED);
    }

    default List<CalendarParticipant> findPendingParticipants(UUID eventId) {
        return findByEventIdAndResponseStatus(eventId, ParticipantResponseStatus.PENDING);
    }

    long countByEventId(UUID eventId);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    boolean existsByEventIdAndExternalEmail(UUID eventId, String externalEmail);
}
