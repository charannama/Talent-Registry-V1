package com.zencube.registry.successstory.repository;

import com.zencube.registry.successstory.entity.SuccessStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuccessStoryRepository extends JpaRepository<SuccessStory, UUID> {

    Optional<SuccessStory> findByApplicationId(UUID applicationId);

    boolean existsByApplicationId(UUID applicationId);

    Page<SuccessStory> findByIsPublicTrueOrderBySelectedAtDesc(Pageable pageable);

    Page<SuccessStory> findByIsFeaturedTrueAndIsPublicTrueOrderBySelectedAtDesc(Pageable pageable);
}
