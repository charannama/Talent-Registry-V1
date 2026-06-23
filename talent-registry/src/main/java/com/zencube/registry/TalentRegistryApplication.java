package com.zencube.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the Talent Registry Spring Boot application.
 *
 * <p>EnableJpaAuditing activates Spring Data JPA's automatic population of
 * {@code createdAt}, {@code updatedAt}, {@code createdBy}, and {@code updatedBy}
 * fields defined in {@link com.zencube.registry.common.BaseEntity}.
 */
@SpringBootApplication
public class TalentRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentRegistryApplication.class, args);
    }
}
