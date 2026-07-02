package com.zencube.registry.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response for notifications")
public class NotificationPageResponse {

    @Schema(description = "List of notifications for the current page")
    private List<NotificationResponse> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Number of elements per page", example = "20")
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "45")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    @Schema(description = "Total count of unread notifications for the user", example = "5")
    private long unreadCount;
}
