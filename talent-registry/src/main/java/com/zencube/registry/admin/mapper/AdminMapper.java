package com.zencube.registry.admin.mapper;

import com.zencube.registry.admin.dto.ConfigResponse;
import com.zencube.registry.admin.dto.FeatureFlagResponse;
import com.zencube.registry.config.dto.SystemConfigResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public ConfigResponse toConfigResponse(SystemConfigResponse config) {
        if (config == null) return null;
        return new ConfigResponse(
            config.getConfigKey(),
            config.getConfigValue(),
            config.getDataType() != null ? config.getDataType().name() : null,
            config.getDescription()
        );
    }

    public FeatureFlagResponse toFeatureFlagResponse(com.zencube.registry.featureflag.dto.FeatureFlagResponse flag) {
        if (flag == null) return null;
        return new FeatureFlagResponse(
            flag.getFlagKey(),
            flag.getEnabled(),
            flag.getAppliesTo() != null ? flag.getAppliesTo().name() : null,
            flag.getDescription()
        );
    }
}
