package com.zencube.registry.calendar.service;

import java.time.Instant;
import java.util.UUID;

public interface CalendarExportService {

    byte[] exportEvent(UUID eventId);

    byte[] exportEvents(Instant start, Instant end);
}
