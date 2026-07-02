package com.zencube.registry.expressinterest.dto;

import java.time.Instant;
import java.util.UUID;

public record FormalRequestResponse(
    UUID id,
    UUID enterpriseId,
    UUID studentId,
    UUID openingId,
    Instant requestedAt
) {}
