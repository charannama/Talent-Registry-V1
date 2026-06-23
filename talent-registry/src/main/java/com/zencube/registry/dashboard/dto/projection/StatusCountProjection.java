package com.zencube.registry.dashboard.dto.projection;

import com.zencube.registry.common.enums.ApplicationStatus;

public interface StatusCountProjection {
    ApplicationStatus getStatus();
    Long getCount();
}
