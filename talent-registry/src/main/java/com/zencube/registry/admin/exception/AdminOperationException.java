package com.zencube.registry.admin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AdminOperationException extends RuntimeException {
    
    public AdminOperationException(String message) {
        super(message);
    }
}
