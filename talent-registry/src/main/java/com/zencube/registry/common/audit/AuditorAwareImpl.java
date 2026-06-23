package com.zencube.registry.common.audit;

import com.zencube.registry.common.constants.Constants;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current auditor (authenticated username or subject) to Spring Data JPA auditing.
 * Returns {@link Constants#SYSTEM_USER} when no authenticated principal is present
 * (e.g., during system-initiated operations or startup).
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(Constants.SYSTEM_USER);
        }

        // Works with both username/password and JWT (OAuth2) principals
        String name = authentication.getName();
        return Optional.ofNullable(name).filter(s -> !s.isBlank())
                .or(() -> Optional.of(Constants.SYSTEM_USER));
    }
}
