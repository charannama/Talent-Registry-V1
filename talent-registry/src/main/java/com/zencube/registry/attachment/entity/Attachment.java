package com.zencube.registry.attachment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attachable_type", nullable = false, updatable = false)
    private String attachableType;

    @Column(name = "attachable_id", nullable = false, updatable = false)
    private UUID attachableId;

    @Column(name = "filename", nullable = false, updatable = false)
    private String filename;

    @Column(name = "content_type", nullable = false, updatable = false)
    private String contentType;

    @Column(name = "size", nullable = false, updatable = false)
    private Long size;

    @Column(name = "storage_path", nullable = false, updatable = false)
    private String storagePath;

    @Column(name = "uploaded_by", nullable = false, updatable = false)
    private UUID uploadedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
