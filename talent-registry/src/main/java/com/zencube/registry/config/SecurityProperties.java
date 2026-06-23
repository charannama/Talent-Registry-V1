package com.zencube.registry.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

    private Lockout lockout = new Lockout();

    @Getter
    @Setter
    public static class Lockout {
        private int maxAttempts = 5;
        private int durationMinutes = 30;
    }
}
