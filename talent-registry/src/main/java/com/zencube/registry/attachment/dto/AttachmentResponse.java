package com.zencube.registry.attachment.dto;

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
public class AttachmentResponse {
    private UUID id;
    private String attachableType;
    private UUID attachableId;
    private String filename;
    private String contentType;
    private Long size;
    private String downloadUrl;
    private UUID uploadedBy;
    private Instant createdAt;
}
