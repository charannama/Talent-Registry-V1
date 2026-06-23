package com.zencube.registry.config.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    private final ConcurrentMapCacheManager concurrentMapCacheManager =
            new ConcurrentMapCacheManager("dashboardMetrics");

    @Bean
    public CacheManager cacheManager() {
        return concurrentMapCacheManager;
    }

    @Scheduled(fixedRateString = "${application.dashboard.cache.ttl:300000}")
    public void evictDashboardMetricsCache() {
        org.springframework.cache.Cache cache = concurrentMapCacheManager.getCache("dashboardMetrics");
        if (cache != null) {
            cache.clear();
        }
    }
}
