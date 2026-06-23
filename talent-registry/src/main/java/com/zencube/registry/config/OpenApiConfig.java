package com.zencube.registry.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger UI configuration.
 *
 * <p>Defines the global API metadata and registers a Bearer-token
 * (JWT) security scheme so that authenticated endpoints can be
 * exercised directly from the Swagger UI.
 *
 * <p>Accessible at:
 * <ul>
 *   <li>http://localhost:8080/swagger-ui/index.html</li>
 *   <li>http://localhost:8080/swagger-ui.html  (redirects to the above)</li>
 *   <li>http://localhost:8080/v3/api-docs  (raw OpenAPI JSON)</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String OAUTH2_SCHEME_NAME = "oauth2Auth";

    @Bean
    public OpenAPI talentRegistryOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme())
                        .addSecuritySchemes(OAUTH2_SCHEME_NAME, oauth2SecurityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Talent Registry API")
                .description("REST API for the Talent Registry platform – manage talent profiles, " +
                             "job openings, applications, and more.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("ZenCube")
                        .email("support@zencube.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://zencube.com"));
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Paste your JWT token (without the 'Bearer ' prefix). " +
                             "Obtain one via the /auth/login endpoint.");
    }

    private SecurityScheme oauth2SecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("OAuth2 Authorization Code Flow")
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .authorizationUrl("/oauth2/authorization/google") // Or parameterized based on provider
                                .tokenUrl("/login/oauth2/code/google")
                                .scopes(new Scopes().addString("openid", "OpenID Connect")
                                                    .addString("profile", "User profile")
                                                    .addString("email", "User email"))));
    }
}
