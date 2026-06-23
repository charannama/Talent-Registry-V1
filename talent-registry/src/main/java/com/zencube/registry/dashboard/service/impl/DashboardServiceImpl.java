package com.zencube.registry.dashboard.service.impl;

import com.zencube.registry.application.repository.ApplicationRepository;
import com.zencube.registry.common.enums.ApplicationStatus;
import com.zencube.registry.dashboard.dto.projection.EnterpriseMetricCountProjection;
import com.zencube.registry.dashboard.dto.projection.EnterpriseStatusCountProjection;
import com.zencube.registry.dashboard.dto.projection.OpeningApplicationCountProjection;
import com.zencube.registry.dashboard.dto.projection.ProcessingTimeProjection;
import com.zencube.registry.dashboard.dto.projection.StatusCountProjection;
import com.zencube.registry.dashboard.dto.response.*;
import com.zencube.registry.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ApplicationRepository applicationRepository;

    @Value("${application.stale-days:14}")
    private int staleDays;

    @Override
    @Transactional(readOnly = true)
    public PipelineMetricsResponse getPipelineMetrics() {
        log.info("Fetching Pipeline Metrics");
        List<StatusCountProjection> statusCounts = applicationRepository.countApplicationsByStatus();
        Map<String, Long> applicationsByStatus = statusCounts.stream()
                .collect(Collectors.toMap(p -> p.getStatus().name(), StatusCountProjection::getCount));

        List<ApplicationStatus> activeStatuses = List.of(
                ApplicationStatus.APPLIED,
                ApplicationStatus.UNDER_REVIEW,
                ApplicationStatus.FORWARDED,
                ApplicationStatus.INTERVIEW_SCHEDULED
        );
        long activePipelineCount = applicationRepository.countApplicationsByStatuses(activeStatuses);

        Instant threshold = Instant.now().minus(staleDays, ChronoUnit.DAYS);
        long staleApplicationsCount = applicationRepository.countStaleApplications(ApplicationStatus.APPLIED, threshold);

        Instant newTimestamp = Instant.now().minus(7, ChronoUnit.DAYS);
        long newApplicationsSinceTimestamp = applicationRepository.countApplicationsCreatedSince(newTimestamp);

        long selectedCandidatesCount = applicationRepository.countByStatus(ApplicationStatus.SELECTED);

        return PipelineMetricsResponse.builder()
                .applicationsByStatus(applicationsByStatus)
                .activePipelineCount(activePipelineCount)
                .staleApplicationsCount(staleApplicationsCount)
                .newApplicationsSinceTimestamp(newApplicationsSinceTimestamp)
                .selectedCandidatesCount(selectedCandidatesCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EnterpriseMetricsResponse getEnterpriseMetrics() {
        log.info("Fetching Enterprise Metrics");

        List<EnterpriseStatusCountProjection> statusCounts = applicationRepository.countApplicationsByStatusPerEnterprise();
        Map<String, Map<String, Long>> applicationsByStatusPerEnterprise = new HashMap<>();
        Map<String, Long> selectionCountPerEnterprise = new HashMap<>();
        Map<String, Long> rejectionCountPerEnterprise = new HashMap<>();
        Map<String, Long> interviewCountPerEnterprise = new HashMap<>();
        Map<String, Long> forwardedCountPerEnterprise = new HashMap<>();

        for (EnterpriseStatusCountProjection proj : statusCounts) {
            String enterpriseName = proj.getEnterpriseName();
            ApplicationStatus status = proj.getStatus();
            Long count = proj.getCount();

            applicationsByStatusPerEnterprise
                    .computeIfAbsent(enterpriseName, k -> new HashMap<>())
                    .put(status.name(), count);

            if (status == ApplicationStatus.SELECTED) selectionCountPerEnterprise.put(enterpriseName, count);
            if (status == ApplicationStatus.REJECTED) rejectionCountPerEnterprise.put(enterpriseName, count);
            if (status == ApplicationStatus.INTERVIEW_SCHEDULED) interviewCountPerEnterprise.put(enterpriseName, count);
            if (status == ApplicationStatus.FORWARDED) forwardedCountPerEnterprise.put(enterpriseName, count);
        }

        List<EnterpriseMetricCountProjection> appCounts = applicationRepository.countApplicationsPerEnterprise();
        Map<String, Long> applicationsPerEnterprise = appCounts.stream()
                .collect(Collectors.toMap(EnterpriseMetricCountProjection::getEnterpriseName, EnterpriseMetricCountProjection::getCount));

        List<ProcessingTimeProjection> procTimeProjections = applicationRepository.calculateAverageProcessingTimePerEnterprise();
        Map<String, Double> averageProcessingTimePerEnterprise = procTimeProjections.stream()
                .collect(Collectors.toMap(ProcessingTimeProjection::getEnterpriseName, ProcessingTimeProjection::getAverageProcessingTimeDays));

        return EnterpriseMetricsResponse.builder()
                .applicationsByStatusPerEnterprise(applicationsByStatusPerEnterprise)
                .applicationsPerEnterprise(applicationsPerEnterprise)
                .selectionCountPerEnterprise(selectionCountPerEnterprise)
                .rejectionCountPerEnterprise(rejectionCountPerEnterprise)
                .interviewCountPerEnterprise(interviewCountPerEnterprise)
                .forwardedCountPerEnterprise(forwardedCountPerEnterprise)
                .averageProcessingTimePerEnterprise(averageProcessingTimePerEnterprise)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OpeningMetricsResponse getOpeningMetrics() {
        log.info("Fetching Opening Metrics");
        List<OpeningApplicationCountProjection> projections = applicationRepository.countApplicationsPerOpening();
        long totalApplications = projections.stream().mapToLong(OpeningApplicationCountProjection::getCount).sum();

        List<OpeningMetricsResponse.OpeningDistributionDto> distributions = projections.stream()
                .map(proj -> OpeningMetricsResponse.OpeningDistributionDto.builder()
                        .openingName(proj.getOpeningName())
                        .enterpriseName(proj.getEnterpriseName())
                        .applicationCount(proj.getCount())
                        .percentage(totalApplications > 0 ? (double) proj.getCount() / totalApplications * 100 : 0)
                        .build())
                .collect(Collectors.toList());

        return OpeningMetricsResponse.builder()
                .openingDistributions(distributions)
                .build();
    }

    private ConversionMetricsResponse getConversionMetrics(long totalApplications, long selectedCount, long rejectedCount, long interviewCount) {
        double overallConversionRate = totalApplications > 0 ? ((double) selectedCount / totalApplications) * 100 : 0;
        long totalProcessed = selectedCount + rejectedCount;
        double selectionRate = totalProcessed > 0 ? ((double) selectedCount / totalProcessed) * 100 : 0;
        double rejectionRate = totalProcessed > 0 ? ((double) rejectedCount / totalProcessed) * 100 : 0;
        double interviewRate = totalApplications > 0 ? ((double) interviewCount / totalApplications) * 100 : 0;
        
        Double overallAverageProcessingTimeDays = applicationRepository.calculateOverallAverageProcessingTimeDays();

        return ConversionMetricsResponse.builder()
                .overallConversionRate(overallConversionRate)
                .selectionRate(selectionRate)
                .rejectionRate(rejectionRate)
                .interviewRate(interviewRate)
                .averageProcessingTimeDays(overallAverageProcessingTimeDays != null ? overallAverageProcessingTimeDays : 0.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardMetrics", key = "'overall'")
    public DashboardAnalyticsResponse getDashboardAnalytics() {
        log.info("Computing Dashboard Analytics (Cache Miss)");

        PipelineMetricsResponse pipelineMetrics = getPipelineMetrics();
        EnterpriseMetricsResponse enterpriseMetrics = getEnterpriseMetrics();
        OpeningMetricsResponse openingMetrics = getOpeningMetrics();

        long totalApplications = pipelineMetrics.getApplicationsByStatus().values().stream().mapToLong(Long::longValue).sum();
        long selectedCount = pipelineMetrics.getApplicationsByStatus().getOrDefault(ApplicationStatus.SELECTED.name(), 0L);
        long rejectedCount = pipelineMetrics.getApplicationsByStatus().getOrDefault(ApplicationStatus.REJECTED.name(), 0L);
        long interviewCount = pipelineMetrics.getApplicationsByStatus().getOrDefault(ApplicationStatus.INTERVIEW_SCHEDULED.name(), 0L);

        ConversionMetricsResponse conversionMetrics = getConversionMetrics(totalApplications, selectedCount, rejectedCount, interviewCount);

        return DashboardAnalyticsResponse.builder()
                .pipelineMetrics(pipelineMetrics)
                .enterpriseMetrics(enterpriseMetrics)
                .openingMetrics(openingMetrics)
                .conversionMetrics(conversionMetrics)
                .generatedAt(Instant.now())
                .build();
    }
}
