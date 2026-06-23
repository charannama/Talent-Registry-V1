package com.zencube.registry.enterprise.service;

import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.repository.OpeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDashboardMetricsService {

    private final OpeningRepository openingRepository;

    @Transactional(readOnly = true)
    public long getLiveOpeningsCount() {
        return openingRepository.countLiveOpenings();
    }

    @Transactional(readOnly = true)
    public long getClosedOpeningsCount() {
        return openingRepository.countClosedOpenings();
    }

    @Transactional(readOnly = true)
    public double getCloseRate() {
        long live = getLiveOpeningsCount();
        long closed = getClosedOpeningsCount();
        long total = live + closed;
        if (total == 0) return 0.0;
        return (double) closed / total * 100.0;
    }

    @Transactional(readOnly = true)
    public double getAverageTimeToCloseHours() {
        List<Opening> closedOpenings = openingRepository.findClosedOpenings();
        if (closedOpenings.isEmpty()) return 0.0;

        long totalHours = 0;
        int validCount = 0;

        for (Opening opening : closedOpenings) {
            if (opening.getPublishedAt() != null && opening.getClosedAt() != null) {
                totalHours += Duration.between(opening.getPublishedAt(), opening.getClosedAt()).toHours();
                validCount++;
            }
        }

        return validCount == 0 ? 0.0 : (double) totalHours / validCount;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardMetrics() {
        return Map.of(
            "liveOpenings", getLiveOpeningsCount(),
            "closedOpenings", getClosedOpeningsCount(),
            "closeRatePercentage", getCloseRate(),
            "averageTimeToCloseHours", getAverageTimeToCloseHours()
        );
    }
}
