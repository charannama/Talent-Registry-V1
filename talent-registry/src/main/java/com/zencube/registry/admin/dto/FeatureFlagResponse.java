package com.zencube.registry.admin.dto;

public record FeatureFlagResponse(
    String key,
    Boolean enabled,
    String appliesTo,
    String description
) {}
