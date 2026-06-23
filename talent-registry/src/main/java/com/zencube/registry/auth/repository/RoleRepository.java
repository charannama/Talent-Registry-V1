package com.zencube.registry.auth.repository;

import com.zencube.registry.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByNameAndDeletedFalse(String name);
    boolean existsByNameAndDeletedFalse(String name);
    
    Optional<Role> findByNameIgnoreCaseAndDeletedFalse(String name);
    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);
    
    java.util.List<Role> findByDeletedFalse();
}
