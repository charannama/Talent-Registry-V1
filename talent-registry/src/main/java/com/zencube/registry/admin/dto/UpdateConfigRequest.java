package com.zencube.registry.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateConfigRequest(
    @NotBlank(message = "Config value cannot be blank")
    String value
) {}
