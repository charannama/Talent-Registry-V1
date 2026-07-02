package com.zencube.registry.journal.dto;

import com.zencube.registry.journal.entity.JournalAction;
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
public class JournalResponse {
    private Long id;
    private String journableType;
    private Long journableId;
    private JournalAction action;
    private UUID actorId;
    private Instant createdAt;
    private List<JournalDetailResponse> changes;
}
