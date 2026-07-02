package com.zencube.registry.featureflag.service;

import com.zencube.registry.featureflag.dto.FeatureFlagResponse;

import java.util.List;

public interface FeatureFlagService {

    boolean isEnabled(String flagKey);

    FeatureFlagResponse enable(String flagKey);

    FeatureFlagResponse disable(String flagKey);

    List<FeatureFlagResponse> getAll();
    
    FeatureFlagResponse getByKey(String flagKey);
}
