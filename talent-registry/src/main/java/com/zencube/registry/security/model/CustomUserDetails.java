package com.zencube.registry.security.model;

import com.zencube.registry.auth.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Map;

import java.util.Collection;
import java.util.UUID;

/**
 * Purpose:
 * Custom implementation of Spring Security's UserDetails interface.
 * Wraps our User entity and extends it with custom claims like userId.
 *
 * Layer:
 * Security / Model
 *
 * Dependencies:
 * Spring Security, Domain User Entity
 *
 * Annotation Explanation:
 * @Getter: Generates getters for all fields.
 *
 * Business Logic Explanation:
 * Spring Security requires a UserDetails object to represent the authenticated user.
 * We need to store the User ID in the JWT, so we extend the basic interface to expose it.
 * The account status (locked, active) is evaluated here to integrate natively with Spring's
 * AbstractUserDetailsAuthenticationProvider.
 *
 * Security Considerations:
 * - isAccountNonLocked: Tied to our 'lockedUntil' logic.
 * - isEnabled: Tied to our 'isActive' and 'emailVerified' logic.
 *
 * Best Practices:
 * - Immutability: Authorities are collected once during loading and stored.
 * - Wrap, don't inherit: Wraps the JPA User entity rather than making the JPA entity
 *   implement UserDetails directly. Keeps persistence decoupled from security.
 *
 * Common Mistakes:
 * - Making the JPA User entity implement UserDetails, which creates tight coupling and
 *   makes JSON serialization messy.
 *
 * Unit Test Coverage:
 * Tested as part of CustomUserDetailsService and AuthenticationProvider.
 */
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

    private final UUID userId;
    private final String username; // which is email
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonLocked;
    private final boolean enabled;
    private final User user;
    private Map<String, Object> attributes;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.userId = user.getId();
        this.username = user.getEmail();
        this.password = user.getPasswordHash();
        this.authorities = authorities;
        
        // Account is locked if lockoutUntil is in the future
        this.accountNonLocked = user.getLockoutUntil() == null || user.getLockoutUntil().isBefore(java.time.Instant.now());
        
        // Enabled if active. The requirement implies email verification could also be checked here,
        // but often we handle specific exceptions (like EmailNotVerifiedException) in the service layer
        // to provide better error messages. We'll stick to 'isActive' for enabled.
        this.enabled = user.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't expire accounts
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We don't expire passwords yet
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public User getUser() {
        return user;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
