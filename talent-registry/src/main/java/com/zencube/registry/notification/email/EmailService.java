package com.zencube.registry.notification.email;

import com.zencube.registry.notification.enums.NotificationEventType;
import java.util.Map;

public interface EmailService {
    void sendEmail(String to, String subject, String body, Boolean isHtml);
    void sendTemplateEmail(String to, NotificationEventType eventType, Map<String, Object> variables);
    boolean validateRecipient(String email);
}
