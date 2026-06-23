package com.zencube.registry.enterprise.service.impl;

import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.enterprise.dto.response.EnterpriseDashboardMetricsResponse;
import com.zencube.registry.enterprise.enums.DateRangeFilter;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import com.zencube.registry.enterprise.service.HrDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrDashboardServiceImpl implements HrDashboardService {

    private final EnterpriseAccountRepository repository;

    @Override
    @Transactional(readOnly = true)
    public EnterpriseDashboardMetricsResponse getEnterpriseMetrics(DateRangeFilter filter, Instant customStart, Instant customEnd) {
        Instant start;
        Instant end = Instant.now();

        if (filter == DateRangeFilter.CUSTOM) {
            if (customStart == null || customEnd == null) {
                throw new BusinessException("Custom date range requires start and end dates", HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE");
            }
            if (customStart.isAfter(customEnd)) {
                throw new BusinessException("Start date cannot be after end date", HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE");
            }
            start = customStart;
            end = customEnd;
        } else {
            start = filter.getStartDate();
        }

        long pending = repository.countByOnboardingStatusAndCreatedAtBetween(EnterpriseOnboardingStatus.PENDING_HR_REVIEW, start, end);
        long approved = repository.countByOnboardingStatusAndCreatedAtBetween(EnterpriseOnboardingStatus.APPROVED, start, end);
        long rejected = repository.countByOnboardingStatusAndCreatedAtBetween(EnterpriseOnboardingStatus.REJECTED, start, end);
        long suspended = repository.countByOnboardingStatusAndCreatedAtBetween(EnterpriseOnboardingStatus.SUSPENDED, start, end);
        
        long total = repository.countByCreatedAtBetween(start, end);
        long totalProcessed = approved + rejected;

        double approvalRate = totalProcessed > 0 ? ((double) approved / totalProcessed) * 100 : 0.0;
        double rejectionRate = totalProcessed > 0 ? ((double) rejected / totalProcessed) * 100 : 0.0;
        double suspensionRate = approved > 0 ? ((double) suspended / (approved + suspended)) * 100 : 0.0; // Approximation of suspension against approvals

        Double avgTimeSeconds = repository.findAverageApprovalTimeInSeconds();
        double avgHours = 0.0;
        double avgDays = 0.0;
        
        if (avgTimeSeconds != null) {
            avgHours = avgTimeSeconds / 3600.0;
            avgDays = avgTimeSeconds / 86400.0;
        }

        return EnterpriseDashboardMetricsResponse.builder()
                .totalEnterprises(total)
                .pendingEnterprises(pending)
                .approvedEnterprises(approved)
                .rejectedEnterprises(rejected)
                .suspendedEnterprises(suspended)
                .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                .rejectionRate(Math.round(rejectionRate * 100.0) / 100.0)
                .suspensionRate(Math.round(suspensionRate * 100.0) / 100.0)
                .averageApprovalTimeHours(Math.round(avgHours * 100.0) / 100.0)
                .averageApprovalTimeDays(Math.round(avgDays * 100.0) / 100.0)
                .dateRange(filter.name())
                .rangeStart(start)
                .rangeEnd(end)
                .generatedAt(Instant.now())
                .build();
    }
}
