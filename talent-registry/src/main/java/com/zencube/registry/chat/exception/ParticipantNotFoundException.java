package com.zencube.registry.chat.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ParticipantNotFoundException extends BusinessException {
    public ParticipantNotFoundException(String message) {
        super(message, HttpStatus.FORBIDDEN, "PARTICIPANT_NOT_FOUND");
    }
}
