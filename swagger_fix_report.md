# Swagger UI Fix Report – Talent Registry

---

## A. Problems Found

| # | File | Problem |
|---|------|---------|
| 1 | `SecurityConfig.java` | **Empty file** – Spring Security auto-blocks every request including all Swagger/OpenAPI routes, returning `401` or `403`. |
| 2 | `SwaggerConfig.java` | **Empty file AND wrong package** – placed 5 levels deep (`config.config.config.SwaggerConfig.SwaggerConfig`). Spring's component scan starts at `com.zencube.registry`, so this class would never be picked up even if it had content. |
| 3 | `application.properties` | **Missing all springdoc properties** – without `springdoc.api-docs.enabled=true` and related settings the UI is still served by the library defaults, but the spec URL mismatches cause a blank/broken Swagger page. |
| 4 | `AuditorConfig.java` | **Empty file** – harmless for Swagger but will cause startup failure when the app actually runs, since `@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")` expects `AuditorAwareImpl` which already exists in `security/`. |

---

## B. Root Cause Analysis

### Why Swagger UI returned 404 / 401
Spring Security, when Spring Boot detects it on the classpath, **secures every URL by default**.
Because `SecurityConfig.java` was empty, Spring Boot fell back to its auto-configuration which blocks all
requests that are not authenticated. The Swagger UI static assets (`/swagger-ui/**`) and the
OpenAPI spec endpoint (`/v3/api-docs`) were never reachable.

### Why APIs were not appearing
Even if the UI loaded, the `OpenAPI` bean (registered in `SwaggerConfig`) was never created because:
1. The file was empty, and
2. The package `com.zencube.registry.config.config.config.SwaggerConfig` is **not** a sub-package of the scan base (`com.zencube.registry`) via the normal directory path – it has intermediate directories named `config` which do not exist as Java packages in the compiled output **unless** the `package` declaration matches. Regardless, no code meant the bean was absent.

### Dependency / version check
`springdoc-openapi-starter-webmvc-ui:2.8.9` is **compatible** with Spring Boot 3.5.x and Spring Security 6.x. ✅ No version change required.

### Component Scanning
`@SpringBootApplication` on `TalentRegistryApplication` in `com.zencube.registry` scans
all sub-packages automatically. The new `OpenApiConfig` placed in `com.zencube.registry.config`
**will be found**.

---

## C. Code Changes Applied

### 1. `SecurityConfig.java` — FIXED

**Path:** `src/main/java/com/zencube/registry/security/config/SecurityConfig.java`

```java
package com.zencube.registry.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### 2. `OpenApiConfig.java` — CREATED (correct package)

**Path:** `src/main/java/com/zencube/registry/config/OpenApiConfig.java`

```java
package com.zencube.registry.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI talentRegistryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Talent Registry API")
                        .description("REST API for the Talent Registry platform")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
```

> [!IMPORTANT]
> The **old broken file** at `config/config/config/SwaggerConfig/SwaggerConfig.java` can be **deleted** — it was empty and in an incorrect package. The new `OpenApiConfig.java` replaces it.

### 3. `application.properties` — UPDATED

```properties
spring.application.name=talent-registry

# SpringDoc / Swagger UI
springdoc.api-docs.enabled=true
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.url=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=alpha
```

---

## D. Swagger Security Whitelist (Summary)

The following paths are configured as `permitAll()` in `SecurityConfig`:

| Path | Purpose |
|------|---------|
| `/swagger-ui/**` | Swagger UI SPA resources |
| `/swagger-ui.html` | Redirect entry point |
| `/v3/api-docs` | OpenAPI JSON spec |
| `/v3/api-docs/**` | OpenAPI sub-paths (e.g. groups) |
| `/v3/api-docs.yaml` | OpenAPI YAML spec |
| `/webjars/**` | Swagger UI static assets (JS/CSS) |

---

## E. Verification Steps

### Step 1 – Clean build
```powershell
cd "c:\Users\layar\Downloads\talent-registry\talent-registry"
./mvnw clean package -DskipTests
```

### Step 2 – Run the application
```powershell
./mvnw spring-boot:run
```
Watch for `Started TalentRegistryApplication` in the console. If you see startup errors, ensure your database is reachable (or comment out `spring.datasource.*` and use H2 for a quick test).

### Step 3 – Test OpenAPI spec (no auth needed)
Open in browser or curl:
```
http://localhost:8080/v3/api-docs
```
Expected: a JSON document starting with `{"openapi":"3.0.x",...}`

### Step 4 – Test Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```
Expected: Swagger UI loads, shows "Talent Registry API v1.0.0", and lists all controller endpoints.

### Step 5 – Verify redirect works
```
http://localhost:8080/swagger-ui.html
```
Expected: Redirects to `swagger-ui/index.html` automatically.

---

## F. Expected Working URLs

| URL | Expected Result |
|-----|----------------|
| `http://localhost:8080/swagger-ui/index.html` | ✅ Swagger UI loads fully |
| `http://localhost:8080/swagger-ui.html` | ✅ Redirects → index.html |
| `http://localhost:8080/v3/api-docs` | ✅ Raw OpenAPI JSON |
| `http://localhost:8080/v3/api-docs.yaml` | ✅ Raw OpenAPI YAML |

---

## G. Troubleshooting Checklist

- [ ] **404 on `/swagger-ui/index.html`**
  → Check that `springdoc-openapi-starter-webmvc-ui` is in `pom.xml`. ✅ It is (v2.8.9).
  → Ensure `springdoc.swagger-ui.enabled=true` in `application.properties`. ✅ Fixed.

- [ ] **Empty Swagger page (UI loads but no APIs shown)**
  → No `OpenAPI` bean found. ✅ Fixed by `OpenApiConfig.java` in `com.zencube.registry.config`.
  → Controller classes must be in sub-packages of `com.zencube.registry` to be scanned.

- [ ] **401/403 on Swagger URLs**
  → `SecurityConfig` was blocking them. ✅ Fixed with `PUBLIC_PATHS` whitelist.

- [ ] **Dependency conflicts**
  → Do NOT add `springfox-swagger2` (older Swagger 2 library). Only keep `springdoc-openapi-starter-webmvc-ui`. ✅ pom.xml is correct.
  → Do NOT mix `springdoc-openapi-ui` (old artifact) with the new `springdoc-openapi-starter-webmvc-ui`.

- [ ] **Incorrect package scanning**
  → `@SpringBootApplication` on `TalentRegistryApplication` in `com.zencube.registry` auto-scans all sub-packages. Any `@Configuration` class must be in `com.zencube.registry.**`. ✅ `OpenApiConfig` is now in `com.zencube.registry.config`.

- [ ] **App fails to start (no auditor bean)**
  → `auditorAwareImpl` bean exists in `AuditorAwareImpl.java`. ✅ OK.
  → If `AuditorConfig.java` must declare extra beans, fill it in; otherwise it can be left empty or deleted.
