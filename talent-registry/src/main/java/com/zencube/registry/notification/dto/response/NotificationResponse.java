package com.zencube.registry.notification.dto.response;

import com.zencube.registry.notification.enums.NotificationEventType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response object representing a single notification")
public class NotificationResponse {

    @Schema(description = "Unique identifier of the notification", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Type of event that triggered the notification", example = "INTERVIEW_SCHEDULED")
    private NotificationEventType eventType;

    @Schema(description = "Type of resource related to the notification", example = "Opening")
    private String resourceType;

    @Schema(description = "Unique identifier of the related resource", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID resourceId;

    @Schema(description = "Short title of the notification", example = "Interview Scheduled")
    private String title;

    @Schema(description = "Detailed body content of the notification", example = "An interview has been scheduled for your application to Software Engineer role.")
    private String body;

    @Schema(description = "Indicates whether the notification has been read", example = "false")
    private boolean read;

    @Schema(description = "Timestamp when the notification was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when the notification was read, or null if unread")
    private Instant readAt;

}
