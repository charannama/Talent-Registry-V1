package com.zencube.registry.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedEnterpriseAccessException extends RuntimeException {
    public UnauthorizedEnterpriseAccessException(String message) {
        super(message);
    }
}
