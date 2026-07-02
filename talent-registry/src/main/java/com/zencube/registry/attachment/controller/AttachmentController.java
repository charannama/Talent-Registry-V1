package com.zencube.registry.attachment.controller;

import com.zencube.registry.attachment.dto.AttachmentResponse;
import com.zencube.registry.attachment.dto.UploadResponse;
import com.zencube.registry.attachment.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "File storage and attachment management APIs")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file attachment", description = "Uploads a file and links it to a polymorphic parent entity.")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("attachableType") String attachableType,
            @RequestParam("attachableId") UUID attachableId,
            @RequestParam("file") MultipartFile file) {
        
        return ResponseEntity.status(201).body(attachmentService.upload(attachableType, attachableId, file));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download attachment", description = "Downloads the physical file content.")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Resource file = attachmentService.download(id);
        AttachmentResponse metadata = attachmentService.getAttachmentMetadata(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .body(file);
    }

    @GetMapping("/{id}/metadata")
    @Operation(summary = "Get attachment metadata", description = "Retrieves metadata without downloading the file.")
    public ResponseEntity<AttachmentResponse> getMetadata(@PathVariable UUID id) {
        return ResponseEntity.ok(attachmentService.getAttachmentMetadata(id));
    }

    @GetMapping("/entity/{attachableType}/{attachableId}")
    @Operation(summary = "Get all attachments for entity", description = "Retrieves all attachment metadata for a given entity.")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsForEntity(
            @PathVariable String attachableType,
            @PathVariable UUID attachableId) {
        return ResponseEntity.ok(attachmentService.getAttachments(attachableType, attachableId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attachment", description = "Physically deletes the file and removes metadata.")
    // Depending on security requirements, you could add method security here:
    // @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(authentication, #id)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        attachmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
