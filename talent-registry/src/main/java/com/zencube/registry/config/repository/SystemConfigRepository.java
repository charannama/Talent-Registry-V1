package com.zencube.registry.config.repository;

import com.zencube.registry.config.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {

    Optional<SystemConfig> findByConfigKey(String key);

    List<SystemConfig> findByConfigKeyStartingWith(String prefix);

    boolean existsByConfigKey(String key);
}
