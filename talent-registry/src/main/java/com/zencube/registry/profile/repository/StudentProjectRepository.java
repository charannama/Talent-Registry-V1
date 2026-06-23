package com.zencube.registry.profile.repository;

import com.zencube.registry.profile.entity.StudentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentProjectRepository extends JpaRepository<StudentProject, UUID> {
    List<StudentProject> findByProfileId(UUID profileId);

    List<StudentProject> findByProfileUserId(UUID userId);

    @Query("SELECT COUNT(p) FROM StudentProject p WHERE p.profile.id = :profileId AND p.completed = true")
    long countCompletedProjects(@Param("profileId") UUID profileId);

    long countByProfileIdAndCompletedTrue(UUID profileId);
}
