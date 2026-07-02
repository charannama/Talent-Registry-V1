package com.zencube.registry.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCommentRequest {
    
    @NotNull(message = "Parent comment ID is required")
    private UUID parentCommentId;
    
    @NotBlank(message = "Reply body is required")
    private String body;
}
