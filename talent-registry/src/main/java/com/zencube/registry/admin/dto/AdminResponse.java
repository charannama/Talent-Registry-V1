package com.zencube.registry.admin.dto;

public record AdminResponse<T>(
    boolean success,
    String message,
    T data
) {}
