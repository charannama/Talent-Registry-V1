package com.zencube.registry.security.config;

import com.zencube.registry.security.filter.JwtAuthenticationFilter;
import com.zencube.registry.security.handler.JwtAccessDeniedHandler;
import com.zencube.registry.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Purpose:
 * Central configuration class for Spring Security. Defines filter chains, 
 * authentication providers, and global security policies.
 *
 * Layer:
 * Security / Config
 *
 * Dependencies:
 * Spring Security Core, Spring Security Web, Spring Boot Web
 *
 * Annotation Explanation:
 * @Configuration: Registers as a Spring Java Config class.
 * @EnableWebSecurity: Switches off default security auto-config and applies custom rules.
 * @EnableMethodSecurity: Enables @PreAuthorize / @PostAuthorize annotations.
 * @RequiredArgsConstructor: Injects JWT filters, handlers, and user details service.
 *
 * Business Logic Explanation:
 * - Disables CSRF (safe because we use stateless JWTs, not cookies).
 * - Disables Sessions (STATELESS policy) to enforce JWT-only auth.
 * - Leaves specific endpoints public (/api/v1/auth/**, /swagger-ui/**, /v3/api-docs/**).
 * - Requires authentication for all other endpoints.
 * - Plugs in our custom JwtAuthenticationEntryPoint and JwtAccessDeniedHandler.
 * - Inserts the JwtAuthenticationFilter *before* the standard UsernamePasswordAuthenticationFilter.
 * - Configures a DaoAuthenticationProvider with a BCryptPasswordEncoder.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/v3/api-docs.yaml",
            "/webjars/**",
            "/api/v1/auth/register",
            "/api/v1/auth/register/hr",
            "/api/v1/auth/register/enterprise",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-verification",
            "/api/v1/auth/password/reset-request",
            "/api/v1/auth/password/reset",
            "/api/v1/enterprises/signup"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    .requestMatchers("/api/v1/audit/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/profile/user/**").hasAuthority("PROFILE_VIEW_ALL")
                    .requestMatchers("/api/v1/talent/**").hasAnyRole("ENTERPRISE_RECRUITER", "HR_STAFF", "ADMIN")
                    .requestMatchers("/api/v1/hr/enterprises/**").hasAnyRole("ENTERPRISE_ADMIN", "HR_STAFF", "ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/enterprises/my/status").hasAnyRole("ENTERPRISE_RECRUITER", "ENTERPRISE")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/enterprises/my").hasRole("ENTERPRISE_RECRUITER")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/openings").hasAuthority("OPENING_CREATE")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/openings/*/submit").hasAuthority("OPENING_SUBMIT")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/openings/*/approve").hasAuthority("OPENING_APPROVE")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/hr/jobs/*/approve").hasAuthority("OPENING_APPROVE")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/openings/*/reject").hasAuthority("OPENING_APPROVE")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/hr/jobs/*/reject").hasAuthority("OPENING_APPROVE")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/openings/*").hasAuthority("OPENING_UPDATE")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/openings/*/close").hasAuthority("OPENING_CLOSE")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/openings/*/archive").hasAuthority("OPENING_ARCHIVE")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/openings/my").hasAuthority("OPENING_VIEW")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/openings/*").hasAuthority("OPENING_VIEW")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/hr/jobs/pending").hasAuthority("OPENING_VIEW_ALL")
                    .requestMatchers("/api/v1/student/**").hasAnyAuthority("OPENING_VIEW_ALL", "ROLE_STUDENT")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/reset-password").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/openings").permitAll()
                    .requestMatchers("/api/v1/enterprises/**").authenticated()
                    .requestMatchers("/api/v1/attachments/**").authenticated()
                    .requestMatchers("/api/v1/comments/**").authenticated()
                    .requestMatchers("/api/v1/activities/**").authenticated()
                    .requestMatchers("/api/v1/tags/**").authenticated()
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/notifications/**").authenticated()
                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
