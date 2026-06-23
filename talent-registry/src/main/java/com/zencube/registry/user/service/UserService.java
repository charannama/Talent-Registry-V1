package com.zencube.registry.user.service;

import com.zencube.registry.user.dto.CreateUserRequest;
import com.zencube.registry.user.dto.UpdateUserRequest;
import com.zencube.registry.user.dto.UserAdminResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for admin-facing User Management operations.
 *
 * <h2>Scope</h2>
 * <p>This interface covers the <em>User Management</em> use case — an admin
 * provisioning, querying, updating, and deactivating user accounts.
 * It is deliberately separate from {@link com.zencube.registry.auth.service.AuthService}
 * which handles the <em>authentication</em> use case (register, login, token refresh).
 *
 * <h2>Future Security</h2>
 * <p>All methods should be protected with {@code @PreAuthorize("hasAuthority('MANAGE_USERS')")}
 * once JWT authentication is wired.
 */
public interface UserService {

    /**
     * Creates and persists a new user account.
     *
     * <p>Business rules:
     * <ol>
     *   <li>Email must be unique across all accounts (including soft-deleted).</li>
     *   <li>Password, if provided, is BCrypt-encoded before persistence.</li>
     *   <li>Default status is {@code PENDING_VERIFICATION}.</li>
     *   <li>LOCAL accounts without a password will have a {@code null} passwordHash.</li>
     * </ol>
     *
     * @param request the creation payload
     * @return the persisted user as a {@link UserAdminResponse}
     * @throws com.zencube.registry.common.exception.ConflictException if an account with the same email exists
     */
    UserAdminResponse createUser(CreateUserRequest request);

    /**
     * Retrieves an active user by UUID.
     *
     * @param id the user's UUID
     * @return the user as a {@link UserAdminResponse}
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if not found or soft-deleted
     */
    UserAdminResponse getUser(UUID id);

    /**
     * Retrieves an active user by email address.
     *
     * @param email the user's email
     * @return the user as a {@link UserAdminResponse}
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if not found or soft-deleted
     */
    UserAdminResponse getUserByEmail(String email);

    /**
     * Returns all active (non-deleted) users in the system.
     *
     * @return list of users, possibly empty
     */
    List<UserAdminResponse> getAllUsers();

    /**
     * Partially updates an existing user account.
     *
     * <p>Only non-null fields in the request are applied.
     * Email, password, and authProvider are immutable via this endpoint.
     *
     * @param id      the UUID of the user to update
     * @param request the update payload
     * @return the updated user as a {@link UserAdminResponse}
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if not found or soft-deleted
     */
    UserAdminResponse updateUser(UUID id, UpdateUserRequest request);

    /**
     * Soft-deletes a user account.
     *
     * <p>The user is marked as deleted rather than physically removed.
     * Related tokens (refresh, verification) remain intact for audit purposes.
     * The user cannot log in after deletion.
     *
     * @param id the UUID of the user to delete
     * @throws com.zencube.registry.common.exception.ResourceNotFoundException if not found or already deleted
     */
    void deleteUser(UUID id);
}
