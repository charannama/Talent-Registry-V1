package com.zencube.registry.expressinterest.dto;

import com.zencube.registry.expressinterest.enums.InterestStage;
import java.util.UUID;

public record InterestSummaryResponse(
    UUID id,
    InterestStage stage
) {}
