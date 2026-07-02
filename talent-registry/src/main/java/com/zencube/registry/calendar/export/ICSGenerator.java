package com.zencube.registry.calendar.export;

import com.zencube.registry.calendar.entity.CalendarEvent;
import com.zencube.registry.calendar.entity.CalendarParticipant;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ICSGenerator {

    public String generateICS(CalendarEvent event, List<CalendarParticipant> participants) {
        return generateICS(List.of(event), List.of(participants));
    }

    public String generateICS(List<CalendarEvent> events, List<List<CalendarParticipant>> participantsList) {
        StringBuilder sb = new StringBuilder();

        appendLine(sb, ICSConstants.BEGIN_VCALENDAR);
        appendLine(sb, ICSConstants.VERSION_2_0);
        appendLine(sb, ICSConstants.PRODID);
        appendLine(sb, ICSConstants.CALSCALE_GREGORIAN);
        appendLine(sb, ICSConstants.METHOD_PUBLISH);

        for (int i = 0; i < events.size(); i++) {
            CalendarEvent event = events.get(i);
            List<CalendarParticipant> participants = participantsList.size() > i ? participantsList.get(i) : List.of();
            appendEvent(sb, event, participants);
        }

        appendLine(sb, ICSConstants.END_VCALENDAR);
        return sb.toString();
    }

    private void appendEvent(StringBuilder sb, CalendarEvent event, List<CalendarParticipant> participants) {
        appendLine(sb, ICSConstants.BEGIN_VEVENT);
        
        // Identity
        appendLine(sb, "UID:event-" + event.getId() + "@zencube.ai");
        appendLine(sb, "DTSTAMP:" + ICSConstants.UTC_FORMATTER.format(Instant.now()));

        // Dates
        if (Boolean.TRUE.equals(event.getAllDayEvent())) {
            appendLine(sb, "DTSTART;VALUE=DATE:" + ICSConstants.DATE_ONLY_FORMATTER.format(event.getStartTime()));
            appendLine(sb, "DTEND;VALUE=DATE:" + ICSConstants.DATE_ONLY_FORMATTER.format(event.getEndTime()));
        } else {
            appendLine(sb, "DTSTART:" + ICSConstants.UTC_FORMATTER.format(event.getStartTime()));
            appendLine(sb, "DTEND:" + ICSConstants.UTC_FORMATTER.format(event.getEndTime()));
        }

        // Core metadata
        appendLine(sb, "SUMMARY:" + escapeText(event.getTitle()));
        
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            appendLine(sb, "DESCRIPTION:" + escapeText(event.getDescription()));
        }
        
        if (event.getLocation() != null && !event.getLocation().isBlank()) {
            appendLine(sb, "LOCATION:" + escapeText(event.getLocation()));
        }

        // Status based on domain rules
        appendLine(sb, "STATUS:" + (event.isUpcoming() ? "CONFIRMED" : "TENTATIVE"));
        appendLine(sb, "SEQUENCE:" + event.getVersion());

        // Audit traces
        if (event.getCreatedAt() != null) {
            appendLine(sb, "CREATED:" + ICSConstants.UTC_FORMATTER.format(event.getCreatedAt()));
        }
        if (event.getUpdatedAt() != null) {
            appendLine(sb, "LAST-MODIFIED:" + ICSConstants.UTC_FORMATTER.format(event.getUpdatedAt()));
        }
        
        // Organizer fallback
        String organizerId = event.getCreatedBy() != null ? event.getCreatedBy().toString() : "ZenCube";
        appendLine(sb, "ORGANIZER;CN=ZenCube Talent:mailto:" + organizerId + "@zencube.ai");

        // Participants
        for (CalendarParticipant p : participants) {
            String email = p.isInternal() && p.getUser() != null ? p.getUser().getEmail() : p.getExternalEmail();
            if (email != null) {
                String cn = p.isInternal() ? "Internal User" : "Guest";
                appendLine(sb, "ATTENDEE;RSVP=TRUE;CN=" + cn + ":mailto:" + email);
            }
        }

        appendLine(sb, ICSConstants.END_VEVENT);
    }

    private void appendLine(StringBuilder sb, String line) {
        // RFC 5545 Line Folding Logic (Max 75 bytes)
        int length = line.length();
        int startIndex = 0;
        
        while (startIndex < length) {
            int endIndex = Math.min(startIndex + ICSConstants.MAX_LINE_LENGTH, length);
            
            // If not the first chunk, prefix with a space
            if (startIndex > 0) {
                sb.append(ICSConstants.FOLDING_SPACE);
                endIndex = Math.min(startIndex + ICSConstants.MAX_LINE_LENGTH - 1, length);
            }
            
            sb.append(line, startIndex, endIndex).append(ICSConstants.CRLF);
            startIndex = endIndex;
        }
    }

    private String escapeText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace(";", "\\;")
                   .replace(",", "\\,")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }
}
