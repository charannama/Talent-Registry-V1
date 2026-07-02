package com.zencube.registry.successstory.service;

import com.zencube.registry.activity.enums.ActivityType;
import com.zencube.registry.activity.service.ActivityService;
import com.zencube.registry.application.entity.Application;
import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.successstory.dto.SuccessStoryResponse;
import com.zencube.registry.successstory.entity.SuccessStory;
import com.zencube.registry.successstory.repository.SuccessStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import com.zencube.registry.notification.event.NotificationEvent;
import com.zencube.registry.notification.enums.NotificationEventType;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuccessStoryServiceImpl implements SuccessStoryService {

    private final SuccessStoryRepository successStoryRepository;
    private final ActivityService activityService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void createFromApplication(Application application) {
        if (application.getId() == null) {
            throw new IllegalArgumentException("Application ID cannot be null");
        }

        // Rule: Duplicate Prevention (One Application -> One Success Story)
        if (successStoryRepository.existsByApplicationId(application.getId())) {
            log.info("Success Story already exists for application: {}", application.getId());
            return;
        }

        // Rule: Snapshot Data Immutability
        String studentName = application.getProfile().getUser().getDisplayName();
        String enterpriseName = application.getOpening().getEnterprise().getCompanyName();
        String openingTitle = application.getOpening().getTitle();

        SuccessStory story = SuccessStory.builder()
                .applicationId(application.getId())
                .studentName(studentName)
                .enterpriseName(enterpriseName)
                .openingTitle(openingTitle)
                .selectedAt(Instant.now())
                .isFeatured(false)
                .isPublic(true) // Public by default
                .build();

        SuccessStory savedStory = successStoryRepository.save(story);
        
        log.info("Auto-generated Success Story for {} at {}", studentName, enterpriseName);

        // Activity Feed Integration
        String description = String.format("%s was selected at %s for %s", studentName, enterpriseName, openingTitle);
        activityService.recordActivity(
                "USER", application.getProfile().getUser().getId().toString(),
                "SUCCESS_STORY", savedStory.getId().toString(),
                ActivityType.SUCCESS_STORY_CREATED,
                description
        );

        // Notification Integration
        try {
            eventPublisher.publishEvent(
                NotificationEvent.builder()
                    .eventType(NotificationEventType.SUCCESS_STORY_CREATED)
                    .recipientId(application.getProfile().getUser().getId())
                    .resourceType("SuccessStory")
                    .resourceId(savedStory.getId())
                    .title("Success Story Created")
                    .message("A success story has been published for your selection at " + enterpriseName)
                    .build()
            );
        } catch(Exception e) {
            log.warn("Failed to send success story notification: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SuccessStoryResponse> getPublicStories(Pageable pageable) {
        return successStoryRepository.findByIsPublicTrueOrderBySelectedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SuccessStoryResponse> getFeaturedStories(Pageable pageable) {
        return successStoryRepository.findByIsFeaturedTrueAndIsPublicTrueOrderBySelectedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "SUCCESS_STORY", idParam = "successStoryId")
    public void updateTestimonial(UUID successStoryId, String testimonial) {
        SuccessStory story = successStoryRepository.findById(successStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Success Story not found"));
        story.setTestimonial(testimonial);
        successStoryRepository.save(story);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "SUCCESS_STORY", idParam = "successStoryId")
    public void featureStory(UUID successStoryId) {
        SuccessStory story = successStoryRepository.findById(successStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Success Story not found"));
        story.setIsFeatured(true);
        successStoryRepository.save(story);
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.UPDATE, entityType = "SUCCESS_STORY", idParam = "successStoryId")
    public void toggleVisibility(UUID successStoryId) {
        SuccessStory story = successStoryRepository.findById(successStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Success Story not found"));
        story.setIsPublic(!story.getIsPublic());
        successStoryRepository.save(story);
    }

    private SuccessStoryResponse mapToResponse(SuccessStory story) {
        return SuccessStoryResponse.builder()
                .id(story.getId())
                .applicationId(story.getApplicationId())
                .studentName(story.getStudentName())
                .enterpriseName(story.getEnterpriseName())
                .openingTitle(story.getOpeningTitle())
                .selectedAt(story.getSelectedAt())
                .testimonial(story.getTestimonial())
                .isFeatured(story.getIsFeatured())
                .isPublic(story.getIsPublic())
                .build();
    }
}
