package com.zencube.registry.tag.repository;

import com.zencube.registry.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    
    Optional<Tag> findByName(String name);
    
    List<Tag> findByCategory(String category);
}
