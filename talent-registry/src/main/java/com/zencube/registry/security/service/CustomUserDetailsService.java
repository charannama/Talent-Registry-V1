package com.zencube.registry.security.service;

import com.zencube.registry.auth.entity.Permission;
import com.zencube.registry.auth.entity.Role;
import com.zencube.registry.auth.entity.User;
import com.zencube.registry.auth.repository.UserRepository;
import com.zencube.registry.rolepermission.entity.RolePermission;
import com.zencube.registry.security.model.CustomUserDetails;
import com.zencube.registry.userrole.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Purpose:
 * Loads user details and their associated roles/permissions from the database
 * to populate the Spring Security Context.
 *
 * Layer:
 * Security / Service
 *
 * Dependencies:
 * UserRepository, Spring Security UserDetailsService
 *
 * Annotation Explanation:
 * @Service: Registers as a Spring service.
 * @RequiredArgsConstructor: For constructor injection.
 * @Transactional(readOnly = true): Crucial here because we are traversing lazy-loaded
 * collections (user.getUserRoles(), role.getRolePermissions()) which requires an active Hibernate Session.
 *
 * Business Logic Explanation:
 * 1. Finds user by email (username).
 * 2. Traverses the User -> UserRole -> Role -> RolePermission -> Permission graph.
 * 3. Maps Roles to SimpleGrantedAuthority with "ROLE_" prefix.
 * 4. Maps Permissions to SimpleGrantedAuthority as-is.
 * 5. Returns CustomUserDetails combining the User entity and authorities.
 *
 * Security Considerations:
 * Throws UsernameNotFoundException instead of returning null, which Spring handles
 * internally. Avoids leaking whether the user exists to unauthorized callers if
 * used in other flows (though here it's mainly for JWT validation and auth).
 *
 * Best Practices:
 * - Flattening RBAC: Translates the nested RBAC model into a flat list of GrantedAuthorities.
 *   This makes Method Security (@PreAuthorize) very simple to write.
 * - Read-only Transactions: Using readOnly = true optimizes JPA session flush modes.
 *
 * Common Mistakes:
 * - Forgetting @Transactional, resulting in LazyInitializationException when accessing userRoles.
 *
 * Unit Test Coverage:
 * Should mock UserRepository and verify the resulting GrantedAuthorities set matches
 * the expected roles and permissions. Test exception thrown when user not found.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Traverse lazy collections. @Transactional keeps the session open.
        for (UserRole userRole : user.getUserRoles()) {
            // Ignore deleted mappings if soft delete is implemented. Assuming BaseEntity has isDeleted()
            if (userRole.isDeleted()) continue;

            Role role = userRole.getRole();
            if (role.isDeleted()) continue;

            // Add Role authority
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));

            // Add Permission authorities
            for (RolePermission rolePermission : role.getRolePermissions()) {
                if (rolePermission.isDeleted()) continue;

                Permission permission = rolePermission.getPermission();
                if (permission.isDeleted()) continue;

                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }

        return new CustomUserDetails(user, authorities);
    }
}
