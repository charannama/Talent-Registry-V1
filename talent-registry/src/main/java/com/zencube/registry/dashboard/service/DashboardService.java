package com.zencube.registry.dashboard.service;

import com.zencube.registry.dashboard.dto.response.DashboardAnalyticsResponse;
import com.zencube.registry.dashboard.dto.response.EnterpriseMetricsResponse;
import com.zencube.registry.dashboard.dto.response.OpeningMetricsResponse;
import com.zencube.registry.dashboard.dto.response.PipelineMetricsResponse;

public interface DashboardService {
    PipelineMetricsResponse getPipelineMetrics();
    EnterpriseMetricsResponse getEnterpriseMetrics();
    OpeningMetricsResponse getOpeningMetrics();
    DashboardAnalyticsResponse getDashboardAnalytics();
}
