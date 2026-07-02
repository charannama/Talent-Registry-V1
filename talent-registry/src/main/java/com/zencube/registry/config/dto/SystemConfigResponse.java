package com.zencube.registry.config.dto;

import com.zencube.registry.config.enums.ConfigDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {
    
    private UUID id;
    private String configKey;
    private String configValue;
    private ConfigDataType dataType;
    private String description;
}
