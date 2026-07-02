package com.zencube.registry.notification.email.model;

import lombok.Builder;

@Builder
public record EmailMessage(
        String to,
        String subject,
        String body,
        Boolean isHtml
) {
    public EmailMessage {
        if (isHtml == null) {
            isHtml = true;
        }
    }
}
