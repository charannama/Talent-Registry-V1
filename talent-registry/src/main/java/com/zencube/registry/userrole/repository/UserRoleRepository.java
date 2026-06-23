package com.zencube.registry.userrole.repository;

import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.userrole.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 1. Purpose
 * Spring Data JPA repository for managing UserRole mappings.
 *
 * 2. Layer
 * Repository / Data Access Layer.
 *
 * 4. Annotation Explanation
 * @Repository: Marks this interface as a Spring Data repository, enabling exception translation.
 *
 * 5. Business Logic Explanation
 * Uses Spring Data JPA conventions for standard queries.
 * Custom @Query is used to perform JPQL fetch joins to prevent N+1 queries when fetching lists.
 * We only want active (non-deleted) mappings, so `is_deleted = false` is enforced.
 *
 * 6. Best Practices
 * - Return Lists using JOIN FETCH when returning multiple entities that contain lazy associations.
 * - Accept full Entity references (User/Role) in parameters rather than primitive IDs to
 *   keep the service layer object-oriented and let Hibernate optimize the foreign-key queries.
 *
 * 7. Common Mistakes
 * - Forgetting `deleted = false` in custom queries can accidentally surface revoked roles.
 * - Relying on default JPA `findAll()` causing N+1 queries for user and role references.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    boolean existsByUserAndRoleAndDeletedFalse(User user, Role role);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user JOIN FETCH ur.role WHERE ur.user = :user AND ur.deleted = false")
    List<UserRole> findByUserAndDeletedFalse(User user);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user JOIN FETCH ur.role WHERE ur.role = :role AND ur.deleted = false")
    List<UserRole> findByRoleAndDeletedFalse(Role role);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user JOIN FETCH ur.role r WHERE r.name = :roleName AND ur.deleted = false")
    List<UserRole> findByRoleNameAndDeletedFalse(String roleName);

    java.util.Optional<UserRole> findByUserAndRoleAndDeletedFalse(User user, Role role);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user JOIN FETCH ur.role WHERE ur.deleted = false")
    List<UserRole> findAllActiveWithDetails();
}
