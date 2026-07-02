package com.zencube.registry.comment.dto;

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
public class CommentResponse {
    private UUID id;
    private String body;
    private UUID authorId;
    private String authorName;
    private UUID parentId;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommentResponse> replies;
}
