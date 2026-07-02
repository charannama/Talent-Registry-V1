package com.zencube.registry.featureflag.service;

import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.featureflag.dto.FeatureFlagResponse;
import com.zencube.registry.featureflag.entity.FeatureFlag;
import com.zencube.registry.featureflag.repository.FeatureFlagRepository;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagServiceImpl implements FeatureFlagService {

    public static final String FEATURE_FLAG_CACHE = "featureFlags";

    private final FeatureFlagRepository featureFlagRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = FEATURE_FLAG_CACHE, key = "#flagKey", cacheManager = "systemConfigCache")
    public boolean isEnabled(String flagKey) {
        log.debug("Checking feature flag status from DB for: {}", flagKey);
        return featureFlagRepository.findByFlagKey(flagKey)
                .map(FeatureFlag::getEnabled)
                .orElse(false); // Default to false if flag doesn't exist
    }

    @Override
    @Transactional
    @CacheEvict(value = FEATURE_FLAG_CACHE, key = "#flagKey", cacheManager = "systemConfigCache")
    @Audited(action = JournalAction.UPDATE, entityType = "FEATURE_FLAG", idParam = "none")
    public FeatureFlagResponse enable(String flagKey) {
        log.info("Enabling feature flag: {}", flagKey);
        FeatureFlag flag = featureFlagRepository.findByFlagKey(flagKey)
                .orElseThrow(() -> new ResourceNotFoundException("Feature Flag not found: " + flagKey));
        
        flag.setEnabled(true);
        return mapToResponse(featureFlagRepository.save(flag));
    }

    @Override
    @Transactional
    @CacheEvict(value = FEATURE_FLAG_CACHE, key = "#flagKey", cacheManager = "systemConfigCache")
    @Audited(action = JournalAction.UPDATE, entityType = "FEATURE_FLAG", idParam = "none")
    public FeatureFlagResponse disable(String flagKey) {
        log.info("Disabling feature flag: {}", flagKey);
        FeatureFlag flag = featureFlagRepository.findByFlagKey(flagKey)
                .orElseThrow(() -> new ResourceNotFoundException("Feature Flag not found: " + flagKey));
        
        flag.setEnabled(false);
        return mapToResponse(featureFlagRepository.save(flag));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getAll() {
        return featureFlagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureFlagResponse getByKey(String flagKey) {
        return featureFlagRepository.findByFlagKey(flagKey)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Feature Flag not found: " + flagKey));
    }

    private FeatureFlagResponse mapToResponse(FeatureFlag flag) {
        return FeatureFlagResponse.builder()
                .id(flag.getId())
                .flagKey(flag.getFlagKey())
                .enabled(flag.getEnabled())
                .description(flag.getDescription())
                .appliesTo(flag.getAppliesTo())
                .build();
    }
}
