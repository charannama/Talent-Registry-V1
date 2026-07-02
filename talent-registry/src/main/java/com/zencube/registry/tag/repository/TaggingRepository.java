package com.zencube.registry.tag.repository;

import com.zencube.registry.tag.entity.Tagging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaggingRepository extends JpaRepository<Tagging, UUID> {
    
    List<Tagging> findByTaggableTypeAndTaggableId(String taggableType, String taggableId);
    
}
