package com.zencube.registry.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class ConfigCacheManager {

    public static final String SYSTEM_CONFIG_CACHE = "systemConfigs";

    @Bean("systemConfigCache")
    public CacheManager systemConfigCache() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SYSTEM_CONFIG_CACHE, "featureFlags");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // 300 seconds TTL
                .maximumSize(1000));
        return cacheManager;
    }
}
