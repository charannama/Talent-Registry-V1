package com.zencube.registry.chat.dto.response;

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
public class ChatParticipantResponse {

    private UUID id;
    private UUID threadId;
    private UUID userId;
    private Instant joinedAt;
    private Instant lastReadAt;

}
