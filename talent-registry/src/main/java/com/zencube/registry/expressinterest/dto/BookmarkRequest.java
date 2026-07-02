package com.zencube.registry.expressinterest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookmarkRequest(
    @NotNull(message = "Student ID cannot be null")
    UUID studentId,
    
    UUID openingId
) {}
