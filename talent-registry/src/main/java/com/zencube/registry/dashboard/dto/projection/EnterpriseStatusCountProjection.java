package com.zencube.registry.dashboard.dto.projection;

import com.zencube.registry.common.enums.ApplicationStatus;

public interface EnterpriseStatusCountProjection {
    String getEnterpriseName();
    ApplicationStatus getStatus();
    Long getCount();
}
