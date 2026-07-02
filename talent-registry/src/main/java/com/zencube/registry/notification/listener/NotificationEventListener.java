package com.zencube.registry.notification.listener;

import com.zencube.registry.notification.event.NotificationEvent;
import com.zencube.registry.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.debug("Received NotificationEvent: {}", event.getEventType());
        try {
            notificationService.processNotificationEvent(event);
        } catch (Exception e) {
            log.error("Failed to process NotificationEvent: {}", event.getEventType(), e);
        }
    }
}
