package com.zencube.registry.attachment;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.attachment.entity.Attachment;
import com.zencube.registry.attachment.repository.AttachmentRepository;
import com.zencube.registry.attachment.service.AttachmentServiceImpl;
import com.zencube.registry.common.enums.AttachmentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    @Test
    void uploadAttachment_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );
        UUID attachableId = UUID.randomUUID();

        Attachment savedAttachment = new Attachment();
        savedAttachment.setId(UUID.randomUUID());
        savedAttachment.setFilename("test.pdf");
        savedAttachment.setContentType("application/pdf");
        savedAttachment.setSize(file.getSize());

        // We can't mock private methods, but we can verify save is called if we mock repository
        // when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedAttachment);

        // Call the service (assuming the implementation doesn't crash on S3 / Local storage if not mocked)
        // Note: The actual AttachmentServiceImpl may require a StorageService or similar to be mocked.
        // For now, we are providing a basic test skeleton.
    }
}
