package com.zencube.registry.calendar.repository;

import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.enums.EventCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID>, JpaSpecificationExecutor<CalendarEvent> {

    List<CalendarEvent> findByEventableTypeAndEventableId(String eventableType, UUID eventableId);

    List<CalendarEvent> findByEventCategory(EventCategory category);

    @Query("SELECT c FROM CalendarEvent c WHERE c.createdBy = :userId AND c.startTime >= :start AND c.startTime <= :end ORDER BY c.startTime ASC")
    Page<CalendarEvent> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT c FROM CalendarEvent c WHERE c.startTime >= :startDate AND c.startTime <= :endDate")
    List<CalendarEvent> findEventsBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT c FROM CalendarEvent c WHERE c.startTime > :now")
    List<CalendarEvent> findUpcomingEvents(@Param("now") Instant now);

    List<CalendarEvent> findByTimezone(String timezone);

    List<CalendarEvent> findByAllDayEventTrue();

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CalendarEvent c " +
           "WHERE c.eventableType = :eventableType " +
           "AND c.eventableId = :eventableId " +
           "AND c.startTime < :end " +
           "AND c.endTime > :start " +
           "AND (:excludeId IS NULL OR c.id != :excludeId)")
    boolean existsConflictingEvent(
            @Param("eventableType") String eventableType, 
            @Param("eventableId") UUID eventableId, 
            @Param("start") Instant start, 
            @Param("end") Instant end,
            @Param("excludeId") UUID excludeId
    );
}
