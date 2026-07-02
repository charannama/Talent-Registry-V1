package com.zencube.registry.attachment.repository;

import com.zencube.registry.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByAttachableTypeAndAttachableId(String attachableType, UUID attachableId);
}
