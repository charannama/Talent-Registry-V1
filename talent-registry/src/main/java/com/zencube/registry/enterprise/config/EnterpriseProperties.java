package com.zencube.registry.enterprise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Purpose: Configuration properties for Enterprise features.
 * Automatically binds properties with prefix "enterprise" from application configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "enterprise")
@Data
public class EnterpriseProperties {
    
    /**
     * List of personal email domains that are blocked from registering as an enterprise.
     */
    private List<String> blockedDomains = List.of(
            "gmail.com",
            "yahoo.com",
            "hotmail.com",
            "outlook.com",
            "live.com",
            "icloud.com",
            "me.com",
            "aol.com",
            "protonmail.com",
            "proton.me",
            "mail.com",
            "zoho.com",
            "yandex.com",
            "gmx.com",
            "rediffmail.com"
    );
}
