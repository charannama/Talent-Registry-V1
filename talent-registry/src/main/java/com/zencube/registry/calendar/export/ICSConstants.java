package com.zencube.registry.calendar.export;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ICSConstants {

    private ICSConstants() {}

    public static final String CRLF = "\r\n";
    public static final String FOLDING_SPACE = " ";
    public static final int MAX_LINE_LENGTH = 75;

    public static final String BEGIN_VCALENDAR = "BEGIN:VCALENDAR";
    public static final String END_VCALENDAR = "END:VCALENDAR";
    public static final String VERSION_2_0 = "VERSION:2.0";
    public static final String PRODID = "PRODID:-//ZenCube//Talent Registry//EN";
    public static final String CALSCALE_GREGORIAN = "CALSCALE:GREGORIAN";
    public static final String METHOD_PUBLISH = "METHOD:PUBLISH";

    public static final String BEGIN_VEVENT = "BEGIN:VEVENT";
    public static final String END_VEVENT = "END:VEVENT";

    public static final String BEGIN_VTIMEZONE = "BEGIN:VTIMEZONE";
    public static final String END_VTIMEZONE = "END:VTIMEZONE";

    public static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC"));
}
