package com.zencube.registry.calendar.dto.response;

import com.zencube.registry.calendar.enums.ParticipantResponseStatus;
import com.zencube.registry.calendar.enums.ParticipantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private UUID participantId;
    private ParticipantType participantType;
    private ParticipantResponseStatus responseStatus;
    private UUID userId;
    private String userEmail;
    private String externalEmail;
    private UUID eventId;
    private Instant createdAt;
    private Instant updatedAt;
}
