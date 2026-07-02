package com.zencube.registry.activity.service;

import com.zencube.registry.activity.dto.ActivityResponse;
import com.zencube.registry.activity.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityService {
    
    /**
     * The central event writer used by other modules to emit activities to the feed.
     */
    void recordActivity(
            String trackableType, 
            String trackableId, 
            String targetType, 
            String targetId, 
            ActivityType activityType, 
            String description
    );

    Page<ActivityResponse> getGlobalFeed(Pageable pageable);

    Page<ActivityResponse> getMyFeed(Pageable pageable);

    Page<ActivityResponse> getEntityFeed(String targetType, String targetId, Pageable pageable);
}
