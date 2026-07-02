package com.zencube.registry.successstory.service;

import com.zencube.registry.application.entity.Application;
import com.zencube.registry.successstory.dto.SuccessStoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SuccessStoryService {

    void createFromApplication(Application application);

    Page<SuccessStoryResponse> getPublicStories(Pageable pageable);

    Page<SuccessStoryResponse> getFeaturedStories(Pageable pageable);

    void updateTestimonial(UUID successStoryId, String testimonial);

    void featureStory(UUID successStoryId);
    
    void toggleVisibility(UUID successStoryId);
}
