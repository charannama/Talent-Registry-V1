package com.zencube.registry.notification.controller;

import com.zencube.registry.auth.entity.User;
import com.zencube.registry.notification.dto.*;
import com.zencube.registry.notification.dto.response.*;
import com.zencube.registry.notification.dto.request.*;
import com.zencube.registry.notification.service.NotificationService;
import com.zencube.registry.notification.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification and alert APIs")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final com.zencube.registry.security.facade.AuthenticationFacade authenticationFacade;

    @GetMapping
    @Operation(summary = "Get All Notifications", description = "Retrieve paginated notifications for current user")
    public ResponseEntity<PaginatedNotificationResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(notificationService.getUserNotifications(authenticationFacade.getCurrentUserId(), page, size));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get Unread Notifications", description = "Retrieve unread paginated notifications")
    public ResponseEntity<PaginatedNotificationResponse> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            
        return ResponseEntity.ok(notificationService.getUnreadNotifications(authenticationFacade.getCurrentUserId(), page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get Unread Count", description = "Get total number of unread notifications")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
            
        return ResponseEntity.ok(notificationService.countUnreadNotifications(authenticationFacade.getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark As Read", description = "Mark a specific notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id) {
            
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark All As Read", description = "Mark all notifications for current user as read")
    public ResponseEntity<Void> markAllAsRead() {
            
        notificationService.markAllAsRead(authenticationFacade.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Notification", description = "Delete a specific notification")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID id) {
            
        notificationService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    @Operation(summary = "Get Settings", description = "Get master notification delivery settings")
    public ResponseEntity<NotificationSettingsResponse> getSettings() {
            
        return ResponseEntity.ok(notificationPreferenceService.getSettings(authenticationFacade.getCurrentUserId()));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update Settings", description = "Update master delivery toggles (email, push, in-app)")
    public ResponseEntity<NotificationSettingsResponse> updateSettings(
            @RequestBody UpdateNotificationSettingsRequest request) {
            
        return ResponseEntity.ok(notificationPreferenceService.updateSettings(authenticationFacade.getCurrentUserId(), request));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get Preferences", description = "Get granular event notification preferences")
    public ResponseEntity<List<NotificationPreferenceResponse>> getPreferences() {
            
        return ResponseEntity.ok(notificationPreferenceService.getPreferences(authenticationFacade.getCurrentUserId()));
    }

    @PutMapping("/preferences/{eventType}")
    @Operation(summary = "Update Preferences", description = "Update granular event notification preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @PathVariable com.zencube.registry.notification.enums.NotificationEventType eventType,
            @RequestBody UpdateNotificationPreferenceRequest request) {
            
        return ResponseEntity.ok(notificationPreferenceService.updatePreference(authenticationFacade.getCurrentUserId(), eventType, request));
    }
}
