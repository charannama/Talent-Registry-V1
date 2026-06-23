package com.zencube.registry.talent.repository;

import com.zencube.registry.talent.entity.TalentProfileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TalentProfileViewRepository extends JpaRepository<TalentProfileView, UUID> {
}
