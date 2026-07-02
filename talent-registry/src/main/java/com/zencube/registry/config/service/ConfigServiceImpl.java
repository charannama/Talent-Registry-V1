package com.zencube.registry.config.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.config.cache.ConfigCacheManager;
import com.zencube.registry.config.dto.SystemConfigResponse;
import com.zencube.registry.config.entity.SystemConfig;
import com.zencube.registry.config.enums.ConfigDataType;
import com.zencube.registry.config.repository.SystemConfigRepository;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true, noRollbackFor = ResourceNotFoundException.class)
    @Cacheable(value = ConfigCacheManager.SYSTEM_CONFIG_CACHE, key = "#key", cacheManager = "systemConfigCache")
    public String get(String key) {
        log.debug("Fetching config for key: {} (Cache Miss)", key);
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElseThrow(() -> new ResourceNotFoundException("Config key not found: " + key));
    }

    @Override
    @Transactional(readOnly = true, noRollbackFor = ResourceNotFoundException.class)
    public <T> T get(String key, Class<T> type) {
        // Internally calls the cached get()
        String rawValue = get(key);
        
        try {
            if (type == String.class) {
                return type.cast(rawValue);
            } else if (type == Integer.class) {
                return type.cast(Integer.valueOf(rawValue));
            } else if (type == Boolean.class) {
                return type.cast(Boolean.valueOf(rawValue));
            } else {
                // Handle JSON -> Java Object
                return objectMapper.readValue(rawValue, type);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON config for key: {}", key, e);
            throw new IllegalArgumentException("Failed to cast config value to " + type.getSimpleName());
        } catch (NumberFormatException e) {
            log.error("Failed to parse Integer config for key: {}", key, e);
            throw new IllegalArgumentException("Failed to cast config value to Integer");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = ConfigCacheManager.SYSTEM_CONFIG_CACHE, key = "#key", cacheManager = "systemConfigCache")
    @Audited(action = JournalAction.UPDATE, entityType = "SYSTEM_CONFIG", idParam = "none") // idParam none since we don't have id in signature
    public void set(String key, Object value, ConfigDataType dataType, String description) {
        log.info("Setting config for key: {}", key);

        String stringValue;
        if (dataType == ConfigDataType.JSON) {
            try {
                stringValue = objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid JSON object provided");
            }
        } else {
            stringValue = String.valueOf(value);
        }

        Optional<SystemConfig> existing = systemConfigRepository.findByConfigKey(key);
        if (existing.isPresent()) {
            SystemConfig config = existing.get();
            config.setConfigValue(stringValue);
            config.setDataType(dataType);
            if (description != null) {
                config.setDescription(description);
            }
            systemConfigRepository.save(config);
        } else {
            SystemConfig config = SystemConfig.builder()
                    .configKey(key)
                    .configValue(stringValue)
                    .dataType(dataType)
                    .description(description)
                    .build();
            systemConfigRepository.save(config);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getAll() {
        return systemConfigRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfigResponse> getByPrefix(String prefix) {
        return systemConfigRepository.findByConfigKeyStartingWith(prefix)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = ConfigCacheManager.SYSTEM_CONFIG_CACHE, key = "#key", cacheManager = "systemConfigCache")
    @Audited(action = JournalAction.DELETE, entityType = "SYSTEM_CONFIG", idParam = "none")
    public void delete(String key) {
        systemConfigRepository.findByConfigKey(key)
                .ifPresent(systemConfigRepository::delete);
    }

    private SystemConfigResponse mapToResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .dataType(config.getDataType())
                .description(config.getDescription())
                .build();
    }
}
