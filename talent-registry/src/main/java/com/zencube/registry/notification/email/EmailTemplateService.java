package com.zencube.registry.notification.email;

import com.zencube.registry.notification.enums.NotificationEventType;
import java.util.Map;

public interface EmailTemplateService {
    String generateSubject(NotificationEventType eventType, Map<String, Object> variables);
    String generateBody(NotificationEventType eventType, Map<String, Object> variables);
}
