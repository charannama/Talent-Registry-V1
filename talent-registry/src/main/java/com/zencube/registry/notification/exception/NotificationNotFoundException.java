package com.zencube.registry.notification.exception;

import com.zencube.registry.common.exception.ResourceNotFoundException;

public class NotificationNotFoundException extends ResourceNotFoundException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
