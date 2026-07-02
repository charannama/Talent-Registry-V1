package com.zencube.registry.admin.dto;

public record ConfigResponse(
    String key,
    String value,
    String dataType,
    String description
) {}
