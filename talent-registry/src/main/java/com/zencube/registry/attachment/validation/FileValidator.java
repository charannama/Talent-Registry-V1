package com.zencube.registry.attachment.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class FileValidator {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB

    // Safe allowed MIME types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg",
            "image/jpg"
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types are PDF, DOC, DOCX, PNG, JPG, JPEG");
        }
    }
}
