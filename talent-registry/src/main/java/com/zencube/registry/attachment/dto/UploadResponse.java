package com.zencube.registry.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private UUID attachmentId;
    private String filename;
    private String downloadUrl;
    private String message;
}
