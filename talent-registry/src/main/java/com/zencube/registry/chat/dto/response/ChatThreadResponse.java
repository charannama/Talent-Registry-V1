package com.zencube.registry.chat.dto.response;

import com.zencube.registry.chat.enums.ThreadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatThreadResponse {

    private UUID id;
    private ThreadType threadType;
    private boolean archived;
    private String contextableType;
    private UUID contextableId;
    
    private Integer unreadCount;
    private String lastMessagePreview;
    private Instant lastMessageAt;
    private Instant createdAt;
    
    private ApplicationContextResponse application;
    
    private UserSummaryResponse createdBy;
    private List<ParticipantSummaryResponse> participants;

}
