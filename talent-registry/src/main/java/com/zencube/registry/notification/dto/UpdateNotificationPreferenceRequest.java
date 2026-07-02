package com.zencube.registry.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update event-specific notification preferences")
public class UpdateNotificationPreferenceRequest {

    @NotNull(message = "Email enabled flag is required")
    @Schema(description = "Enable or disable email notifications for this event", example = "true")
    private Boolean emailEnabled;

    @NotNull(message = "Push enabled flag is required")
    @Schema(description = "Enable or disable push notifications for this event", example = "false")
    private Boolean pushEnabled;

    @NotNull(message = "In-App enabled flag is required")
    @Schema(description = "Enable or disable in-app notifications for this event", example = "true")
    private Boolean inAppEnabled;
}
