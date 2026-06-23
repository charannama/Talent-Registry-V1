package com.zencube.registry.profile.repository;

import com.zencube.registry.profile.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID>, JpaSpecificationExecutor<StudentProfile> {
    Optional<StudentProfile> findByUserId(UUID userId);
}
