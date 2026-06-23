package com.zencube.registry.common.exception;

public class DuplicateRoleException extends ConflictException {
    public DuplicateRoleException(String message) {
        super(message);
    }
}
