package com.zencube.registry.activity.repository;

import com.zencube.registry.activity.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    // Global Feed
    Page<Activity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Feed by user/actor
    Page<Activity> findByTrackableIdOrderByCreatedAtDesc(String trackableId, Pageable pageable);

    // Feed by target entity
    Page<Activity> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId, Pageable pageable);
}
