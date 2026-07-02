package com.zencube.registry.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with Notification Preferences.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotificationPreferenceException extends RuntimeException {

    public NotificationPreferenceException(String message) {
        super(message);
    }

    public NotificationPreferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
