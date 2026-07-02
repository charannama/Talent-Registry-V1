package com.zencube.registry.notification.dto.request;

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
@Schema(description = "Request to update global notification settings")
public class UpdateNotificationSettingsRequest {

    @NotNull(message = "Email enabled flag is required")
    @Schema(description = "Enable or disable global email notifications", example = "true")
    private Boolean emailEnabled;

    @NotNull(message = "Push enabled flag is required")
    @Schema(description = "Enable or disable global push notifications", example = "false")
    private Boolean pushEnabled;

    @NotNull(message = "In-App enabled flag is required")
    @Schema(description = "Enable or disable global in-app notifications", example = "true")
    private Boolean inAppEnabled;
}
