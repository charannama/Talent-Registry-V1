package com.zencube.registry.notification.exception;

import com.zencube.registry.common.exception.ForbiddenException;

public class NotificationAccessDeniedException extends ForbiddenException {
    public NotificationAccessDeniedException(String message) {
        super(message);
    }
}
