package com.zencube.registry.enterprise.service.impl;

import com.zencube.registry.common.exception.BusinessException;
import com.zencube.registry.enterprise.dto.response.EnterpriseDashboardMetricsResponse;
import com.zencube.registry.enterprise.enums.DateRangeFilter;
import com.zencube.registry.enterprise.enums.EnterpriseOnboardingStatus;
import com.zencube.registry.enterprise.repository.EnterpriseAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HrDashboardServiceImplTest {

    @Mock
    private EnterpriseAccountRepository repository;

    @InjectMocks
    private HrDashboardServiceImpl dashboardService;

    private Instant start;
    private Instant end;

    @BeforeEach
    void setUp() {
        start = Instant.now().minus(30, ChronoUnit.DAYS);
        end = Instant.now();
    }

    @Test
    void getEnterpriseMetrics_ShouldCalculateMetricsCorrectly() {
        // Arrange
        when(repository.countByOnboardingStatusAndCreatedAtBetween(eq(EnterpriseOnboardingStatus.PENDING_HR_REVIEW), any(), any()))
                .thenReturn(10L);
        when(repository.countByOnboardingStatusAndCreatedAtBetween(eq(EnterpriseOnboardingStatus.APPROVED), any(), any()))
                .thenReturn(80L);
        when(repository.countByOnboardingStatusAndCreatedAtBetween(eq(EnterpriseOnboardingStatus.REJECTED), any(), any()))
                .thenReturn(20L);
        when(repository.countByOnboardingStatusAndCreatedAtBetween(eq(EnterpriseOnboardingStatus.SUSPENDED), any(), any()))
                .thenReturn(5L);
        when(repository.countByCreatedAtBetween(any(), any()))
                .thenReturn(115L);
        
        // 48 hours = 172800 seconds
        when(repository.findAverageApprovalTimeInSeconds()).thenReturn(172800.0);

        // Act
        EnterpriseDashboardMetricsResponse response = dashboardService.getEnterpriseMetrics(DateRangeFilter.ALL_TIME, null, null);

        // Assert
        assertEquals(115L, response.getTotalEnterprises());
        assertEquals(10L, response.getPendingEnterprises());
        assertEquals(80L, response.getApprovedEnterprises());
        assertEquals(20L, response.getRejectedEnterprises());
        assertEquals(5L, response.getSuspendedEnterprises());

        // totalProcessed = 80 + 20 = 100
        assertEquals(80.0, response.getApprovalRate());
        assertEquals(20.0, response.getRejectionRate());
        
        // suspensionRate = 5 / (80 + 5) * 100 = 5 / 85 * 100 = 5.88
        assertEquals(5.88, response.getSuspensionRate(), 0.01);

        assertEquals(48.0, response.getAverageApprovalTimeHours());
        assertEquals(2.0, response.getAverageApprovalTimeDays());
        assertEquals("ALL_TIME", response.getDateRange());
    }

    @Test
    void getEnterpriseMetrics_WithZeroData_ShouldNotDivideByZero() {
        // Arrange
        when(repository.countByOnboardingStatusAndCreatedAtBetween(any(), any(), any())).thenReturn(0L);
        when(repository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(repository.findAverageApprovalTimeInSeconds()).thenReturn(null);

        // Act
        EnterpriseDashboardMetricsResponse response = dashboardService.getEnterpriseMetrics(DateRangeFilter.TODAY, null, null);

        // Assert
        assertEquals(0L, response.getTotalEnterprises());
        assertEquals(0.0, response.getApprovalRate());
        assertEquals(0.0, response.getRejectionRate());
        assertEquals(0.0, response.getSuspensionRate());
        assertEquals(0.0, response.getAverageApprovalTimeDays());
    }

    @Test
    void getEnterpriseMetrics_CustomFilter_WithInvalidDates_ShouldThrowException() {
        // Act & Assert
        BusinessException exception1 = assertThrows(BusinessException.class, () ->
                dashboardService.getEnterpriseMetrics(DateRangeFilter.CUSTOM, null, end));
        assertEquals("INVALID_DATE_RANGE", exception1.getErrorCode());

        BusinessException exception2 = assertThrows(BusinessException.class, () ->
                dashboardService.getEnterpriseMetrics(DateRangeFilter.CUSTOM, end, start));
        assertEquals("INVALID_DATE_RANGE", exception2.getErrorCode());
    }
}
