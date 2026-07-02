package com.zencube.registry.attachment.service;

import com.zencube.registry.attachment.dto.AttachmentResponse;
import com.zencube.registry.attachment.dto.UploadResponse;
import com.zencube.registry.attachment.entity.Attachment;
import com.zencube.registry.attachment.repository.AttachmentRepository;
import com.zencube.registry.attachment.storage.FileStorageService;
import com.zencube.registry.attachment.validation.FileValidator;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;

    @Override
    @Transactional
    public UploadResponse upload(String attachableType, UUID attachableId, MultipartFile file) {
        log.info("Uploading attachment for type={} id={}", attachableType, attachableId);
        
        fileValidator.validate(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "unnamed_file";
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFilename = UUID.randomUUID() + extension;
        String storagePath = fileStorageService.store(file, uniqueFilename);

        Attachment attachment = Attachment.builder()
                .attachableType(attachableType)
                .attachableId(attachableId)
                .filename(originalFilename)
                .contentType(file.getContentType())
                .size(file.getSize())
                .storagePath(storagePath)
                .uploadedBy(getCurrentUserId())
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);

        return UploadResponse.builder()
                .attachmentId(savedAttachment.getId())
                .filename(originalFilename)
                .downloadUrl("/api/v1/attachments/" + savedAttachment.getId())
                .message("File uploaded successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource download(UUID attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);
        return fileStorageService.load(attachment.getStoragePath());
    }

    @Override
    @Transactional
    public void delete(UUID attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);

        // Security check could be handled here or at controller level 
        // using @PreAuthorize with a custom bean method

        fileStorageService.delete(attachment.getStoragePath());
        attachmentRepository.delete(attachment);
        log.info("Deleted attachment id={}", attachmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(String attachableType, UUID attachableId) {
        return attachmentRepository.findByAttachableTypeAndAttachableId(attachableType, attachableId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachmentMetadata(UUID attachmentId) {
        Attachment attachment = findAttachmentOrThrow(attachmentId);
        return mapToResponse(attachment);
    }

    private Attachment findAttachmentOrThrow(UUID id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + id));
    }

    private AttachmentResponse mapToResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .attachableType(attachment.getAttachableType())
                .attachableId(attachment.getAttachableId())
                .filename(attachment.getFilename())
                .contentType(attachment.getContentType())
                .size(attachment.getSize())
                .downloadUrl("/api/v1/attachments/" + attachment.getId())
                .uploadedBy(attachment.getUploadedBy())
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new SecurityException("Upload requires an authenticated user.");
        }
        return UUID.fromString(auth.getName());
    }
}
