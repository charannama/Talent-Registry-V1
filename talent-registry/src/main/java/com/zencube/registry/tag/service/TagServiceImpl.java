package com.zencube.registry.tag.service;

import com.zencube.registry.common.exception.ResourceNotFoundException;
import com.zencube.registry.journal.annotation.Audited;
import com.zencube.registry.journal.entity.JournalAction;
import com.zencube.registry.tag.dto.CreateTagRequest;
import com.zencube.registry.tag.dto.TagResponse;
import com.zencube.registry.tag.entity.Tag;
import com.zencube.registry.tag.entity.Tagging;
import com.zencube.registry.tag.repository.TagRepository;
import com.zencube.registry.tag.repository.TaggingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TaggingRepository taggingRepository;

    @Override
    @Transactional
    @Audited(action = JournalAction.CREATE, entityType = "TAG", idParam = "none")
    public TagResponse createTag(CreateTagRequest request) {
        log.info("Creating tag: {}", request.getName());
        
        // Case insensitive duplicate check
        Optional<Tag> existing = tagRepository.findByName(request.getName().trim());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Tag with name '" + request.getName() + "' already exists");
        }
        
        Tag tag = Tag.builder()
                .name(request.getName().trim())
                .category(request.getCategory())
                .build();
                
        return mapToResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public Tag findOrCreateTag(String name, String category) {
        return tagRepository.findByName(name.trim())
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .name(name.trim())
                            .category(category)
                            .build();
                    return tagRepository.save(newTag);
                });
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.CREATE, entityType = "TAGGING", idParam = "entityId")
    public void tagEntity(String entityType, String entityId, UUID tagId) {
        log.info("Tagging {} #{} with tag {}", entityType, entityId, tagId);
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
                
        // Validation: Ensure valid entity types are used
        validateEntityType(entityType);
        
        Tagging tagging = Tagging.builder()
                .tag(tag)
                .taggableType(entityType.toUpperCase())
                .taggableId(entityId)
                .build();
                
        try {
            taggingRepository.save(tagging);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Entity is already tagged with this tag");
        }
    }

    @Override
    @Transactional
    @Audited(action = JournalAction.DELETE, entityType = "TAGGING", idParam = "entityId")
    public void untagEntity(String entityType, String entityId, UUID tagId) {
        log.info("Removing tag {} from {} #{}", tagId, entityType, entityId);
        
        List<Tagging> taggings = taggingRepository.findByTaggableTypeAndTaggableId(entityType.toUpperCase(), entityId);
        taggings.stream()
                .filter(t -> t.getTag().getId().equals(tagId))
                .findFirst()
                .ifPresent(taggingRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsForEntity(String entityType, String entityId) {
        return taggingRepository.findByTaggableTypeAndTaggableId(entityType.toUpperCase(), entityId)
                .stream()
                .map(Tagging::getTag)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByCategory(String category) {
        return tagRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private void validateEntityType(String entityType) {
        List<String> validTypes = List.of("STUDENT", "OPENING", "ENTERPRISE", "APPLICATION");
        if (!validTypes.contains(entityType.toUpperCase())) {
            throw new IllegalArgumentException("Unsupported taggable entity type: " + entityType);
        }
    }

    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .category(tag.getCategory())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
