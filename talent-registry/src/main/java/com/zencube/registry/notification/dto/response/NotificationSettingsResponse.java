package com.zencube.registry.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing global notification settings")
public class NotificationSettingsResponse {

    @Schema(description = "User ID associated with the settings")
    private UUID userId;

    @Schema(description = "Whether global email notifications are enabled", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "Whether global push notifications are enabled", example = "false")
    private Boolean pushEnabled;

    @Schema(description = "Whether global in-app notifications are enabled", example = "true")
    private Boolean inAppEnabled;
}
