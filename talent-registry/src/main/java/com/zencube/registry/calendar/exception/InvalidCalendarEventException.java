package com.zencube.registry.calendar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCalendarEventException extends RuntimeException {
    public InvalidCalendarEventException(String message) {
        super(message);
    }
}
