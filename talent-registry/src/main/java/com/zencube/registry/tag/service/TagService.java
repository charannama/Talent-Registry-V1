package com.zencube.registry.tag.service;

import com.zencube.registry.tag.dto.CreateTagRequest;
import com.zencube.registry.tag.dto.TagResponse;
import com.zencube.registry.tag.entity.Tag;

import java.util.List;
import java.util.UUID;

public interface TagService {

    TagResponse createTag(CreateTagRequest request);

    Tag findOrCreateTag(String name, String category);

    void tagEntity(String entityType, String entityId, UUID tagId);

    void untagEntity(String entityType, String entityId, UUID tagId);

    List<TagResponse> getTagsForEntity(String entityType, String entityId);

    List<TagResponse> getTagsByCategory(String category);
    
    List<TagResponse> getAllTags();
}
