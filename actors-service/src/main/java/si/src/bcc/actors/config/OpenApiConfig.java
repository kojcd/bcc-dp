package si.src.bcc.actors.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI actorsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Actors Service API")
                        .description("REST API for managing actors and their movies")
                        .version("1.0")
                        .contact(new Contact()
                                .name("BCC Team")
                                .email("damjan.kojc@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}