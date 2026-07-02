package com.zencube.registry.expressinterest.dto;

import com.zencube.registry.expressinterest.enums.InterestStage;
import java.time.Instant;
import java.util.UUID;

public record InterestResponse(
    UUID id,
    UUID enterpriseId,
    UUID studentId,
    UUID openingId,
    InterestStage stage,
    Instant requestedAt,
    Instant createdAt
) {}
