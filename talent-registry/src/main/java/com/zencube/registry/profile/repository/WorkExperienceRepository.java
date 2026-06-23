package com.zencube.registry.profile.repository;

import com.zencube.registry.profile.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, UUID> {

    List<WorkExperience> findByProfileUserIdOrderByStartDateDesc(UUID userId);

}
