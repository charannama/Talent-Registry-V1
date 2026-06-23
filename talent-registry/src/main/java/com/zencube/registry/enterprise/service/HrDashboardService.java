package com.zencube.registry.enterprise.service;

import com.zencube.registry.enterprise.dto.response.EnterpriseDashboardMetricsResponse;
import com.zencube.registry.enterprise.enums.DateRangeFilter;

import java.time.Instant;

public interface HrDashboardService {

    EnterpriseDashboardMetricsResponse getEnterpriseMetrics(DateRangeFilter filter, Instant customStart, Instant customEnd);
}
