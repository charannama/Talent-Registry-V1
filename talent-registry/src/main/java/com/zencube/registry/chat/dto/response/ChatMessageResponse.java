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
public class ChatMessageResponse {

    private UUID id;
    private UUID threadId;
    private UserSummaryResponse sender;
    private String content;
    private Boolean deleted;
    private Instant sentAt;
    private Instant deletedAt;

}
