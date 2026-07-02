package com.zencube.registry.successstory.dto;

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
public class SuccessStoryResponse {
    
    private UUID id;
    private UUID applicationId;
    private String studentName;
    private String enterpriseName;
    private String openingTitle;
    private Instant selectedAt;
    private String testimonial;
    private Boolean isFeatured;
    private Boolean isPublic;
}
