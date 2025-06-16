package si.src.bcc.actors.config;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class OpenApiConfig {

    @Bean
    public PropertyCustomizer dateTimeCustomizer() {
        return (property, type) -> {
            if (type.equals(LocalDateTime.class)) {
                return new Schema<String>()
                        .type("string")
                        .format("date-time")
                        .example("2025-06-16T14:50:57.543Z");
            } else if (type.equals(LocalDate.class)) {
                return new Schema<String>()
                        .type("string")
                        .format("date")
                        .example("2025-06-16");
            }
            return property;
        };
    }

    @Bean
    public OpenAPI actorsOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("BCC Actors Service API")
                        .description("REST API for managing actors (and their movies)")
                        .version("1.0")
                        .contact(new Contact()
                                .name("BCC Team")
                                .email("damjan.kojc@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}