package ar.edu.utn.frc.mycar.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the MyCar REST API.
 *
 * <p>Registers a global {@code bearerAuth} security scheme so that every
 * protected endpoint shows the "Authorize" button in the Swagger UI.
 * The scheme follows RFC 6750: the client sends the token in the
 * {@code Authorization: Bearer <token>} header.</p>
 *
 * <p>Swagger UI is available at: {@code http://localhost:8080/swagger-ui/index.html}<br>
 * Raw OpenAPI spec at: {@code http://localhost:8080/v3/api-docs}</p>
 */
@Configuration
public class OpenApiConfig {

    /** Security scheme name referenced by {@code @SecurityRequirement} on individual operations. */
    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Builds the top-level {@link OpenAPI} descriptor with API metadata and the
     * JWT Bearer security scheme applied globally to all operations.
     *
     * @return configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyCar API")
                        .description("Vehicle management REST API — register, authenticate, and manage vehicles, expenses, and maintenance records.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MyCar Team")
                                .url("https://github.com/412010-PIOTTI/MyCar")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT token obtained from /api/auth/register or /api/auth/login.")));
    }
}
