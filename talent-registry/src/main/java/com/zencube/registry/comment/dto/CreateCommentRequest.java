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
public class CreateCommentRequest {
    
    @NotBlank(message = "Commentable type is required")
    private String commentableType;
    
    @NotNull(message = "Commentable ID is required")
    private UUID commentableId;
    
    @NotBlank(message = "Comment body is required")
    private String body;
}
