package com.zencube.registry.featureflag.dto;

import com.zencube.registry.featureflag.enums.FeatureAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagResponse {
    private UUID id;
    private String flagKey;
    private Boolean enabled;
    private String description;
    private FeatureAudience appliesTo;
}
