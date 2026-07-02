package com.zencube.registry.activity.service;

import com.zencube.registry.activity.dto.ActivityResponse;
import com.zencube.registry.activity.entity.Activity;
import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.repository.ActivityRepository;
import com.zencube.registry.activity.dto.ActorInfo;
import com.zencube.registry.activity.dto.TargetInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    @Override
    @Transactional
    public void recordActivity(String trackableType, String trackableId, String targetType, String targetId, ActivityType activityType, String description) {
        log.info("Recording activity: {} performed {} on {} #{}", trackableType, activityType, targetType, targetId);
        
        Activity activity = Activity.builder()
                .trackableType(trackableType)
                .trackableId(trackableId)
                .targetType(targetType)
                .targetId(targetId)
                .activityType(activityType)
                .description(description)
                .build();
                
        activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getGlobalFeed(Pageable pageable) {
        return activityRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getMyFeed(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new SecurityException("Authentication required to view feed.");
        }
        
        String userId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        if (isAdmin) {
            return getGlobalFeed(pageable);
        }
        
        // For Students, HR, and Recruiters, currently returning activities they initiated or participated in.
        // Advanced role-specific repository queries (like assigned apps) would be routed here.
        return activityRepository.findByTrackableIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityResponse> getEntityFeed(String targetType, String targetId, Pageable pageable) {
        return activityRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId, pageable)
                .map(this::mapToResponse);
    }
    
    private ActivityResponse mapToResponse(Activity activity) {
        return ActivityResponse.builder()
                .id(activity.getId())
                .activityType(activity.getActivityType())
                .description(activity.getDescription())
                .createdAt(activity.getCreatedAt())
                .actor(ActorInfo.builder()
                        .id(activity.getTrackableId())
                        .type(activity.getTrackableType())
                        .name("Actor Name Hidden") // Placeholder until User service integration
                        .build())
                .target(TargetInfo.builder()
                        .id(activity.getTargetId())
                        .type(activity.getTargetType())
                        .displayName(activity.getTargetType() + " #" + activity.getTargetId())
                        .build())
                .build();
    }
}
