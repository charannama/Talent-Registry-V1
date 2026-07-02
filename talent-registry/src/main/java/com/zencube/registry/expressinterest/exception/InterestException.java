package com.zencube.registry.expressinterest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.zencube.registry.common.exception.BadRequestException;

public class InterestException extends BadRequestException {
    
    public InterestException(String message) {
        super(message);
    }
}
