package com.zencube.registry.common.exception;

public class RoleAlreadyAssignedException extends ConflictException {
    public RoleAlreadyAssignedException(String message) {
        super(message);
    }
}
