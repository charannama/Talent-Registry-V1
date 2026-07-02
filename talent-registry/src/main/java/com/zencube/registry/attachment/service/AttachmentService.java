package com.zencube.registry.attachment.service;

import com.zencube.registry.attachment.dto.AttachmentResponse;
import com.zencube.registry.attachment.dto.UploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    
    UploadResponse upload(String attachableType, UUID attachableId, MultipartFile file);

    Resource download(UUID attachmentId);

    void delete(UUID attachmentId);

    List<AttachmentResponse> getAttachments(String attachableType, UUID attachableId);
    
    AttachmentResponse getAttachmentMetadata(UUID attachmentId);
}
