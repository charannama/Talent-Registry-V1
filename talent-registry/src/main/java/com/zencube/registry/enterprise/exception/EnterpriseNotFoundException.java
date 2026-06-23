package com.zencube.registry.enterprise.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EnterpriseNotFoundException extends RuntimeException {
    public EnterpriseNotFoundException(String message) {
        super(message);
    }
}
