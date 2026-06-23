package com.zencube.registry.enterprise.validation;

import com.zencube.registry.enterprise.config.EnterpriseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.IDN;

/**
 * Purpose: Validates email domains for Enterprise Signup.
 * Features:
 * - Rejects domains in the blocked-domains list.
 * - Handles IDN (Internationalized Domain Names) homograph attacks.
 * - Prevents subdomain bypass (e.g., user@mail.gmail.com).
 * - Case and whitespace insensitive.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDomainValidator {

    private final EnterpriseProperties properties;

    /**
     * Validates if the provided email has a legitimate enterprise domain.
     * @param email the email address to validate
     * @return true if valid, false if the domain is blocked
     */
    public boolean isValidEnterpriseDomain(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        email = email.trim();
        int atIndex = email.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return false; // Invalid email format
        }

        String rawDomain = email.substring(atIndex + 1);

        String normalizedDomain;
        try {
            // IDN.toASCII normalizes unicode domains to ascii (punycode)
            // also handles converting to lowercase in standard way
            normalizedDomain = IDN.toASCII(rawDomain.toLowerCase()).toLowerCase();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid IDN domain format detected: {}", rawDomain);
            return false; // Invalid unicode domain
        }

        // Check against the configured blocked domains
        for (String blocked : properties.getBlockedDomains()) {
            String normalizedBlocked;
            try {
                normalizedBlocked = IDN.toASCII(blocked.toLowerCase()).toLowerCase();
            } catch (IllegalArgumentException e) {
                // Should not happen with valid config, but safe fallback
                normalizedBlocked = blocked.toLowerCase();
            }

            // Exact match or subdomain match
            if (normalizedDomain.equals(normalizedBlocked) || 
                normalizedDomain.endsWith("." + normalizedBlocked)) {
                return false;
            }
        }

        return true;
    }
}
