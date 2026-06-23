package com.zencube.registry.auth.repository;

import com.zencube.registry.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Permission} entities.
 *
 * <p>All query methods apply the soft-delete filter ({@code deleted = false})
 * so that logically-deleted permissions are never returned by default.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Returns an active (non-deleted) permission by its unique machine code.
     *
     * @param code the permission code to look up (e.g. "VIEW_STUDENTS")
     * @return the matching permission, or {@link Optional#empty()} if absent or soft-deleted
     */
    Optional<Permission> findByCodeAndDeletedFalse(String code);

    /**
     * Returns an active (non-deleted) permission by its human-readable name.
     *
     * @param name the permission name to look up (e.g. "View Students")
     * @return the matching permission, or {@link Optional#empty()} if absent or soft-deleted
     */
    Optional<Permission> findByNameAndDeletedFalse(String name);

    /**
     * Checks whether an active permission with the given code already exists.
     *
     * @param code the permission code to check
     * @return {@code true} if a non-deleted permission with this code exists
     */
    boolean existsByCodeAndDeletedFalse(String code);

    /**
     * Checks whether an active permission with the given name already exists.
     *
     * @param name the permission name to check
     * @return {@code true} if a non-deleted permission with this name exists
     */
    boolean existsByNameAndDeletedFalse(String name);

    /**
     * Returns all active (non-deleted) permissions.
     *
     * @return list of non-deleted permissions, possibly empty
     */
    List<Permission> findAllByDeletedFalse();
}
