package com.zencube.registry.config.service;

import com.zencube.registry.config.dto.SystemConfigResponse;

import java.util.List;

public interface ConfigService {

    String get(String key);

    <T> T get(String key, Class<T> type);

    void set(String key, Object value, com.zencube.registry.config.enums.ConfigDataType dataType, String description);

    List<SystemConfigResponse> getAll();

    List<SystemConfigResponse> getByPrefix(String prefix);
    
    void delete(String key);
}
