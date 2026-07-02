package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class FeatureDisabledException extends RuntimeException {
    
    public FeatureDisabledException(String message) {
        super(message);
    }
}
