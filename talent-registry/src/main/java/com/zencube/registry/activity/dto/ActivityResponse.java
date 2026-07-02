package com.zencube.registry.activity.dto;

import com.zencube.registry.activity.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private UUID id;
    private ActivityType activityType;
    private String description;
    private Instant createdAt;
    private ActorInfo actor;
    private TargetInfo target;
}
