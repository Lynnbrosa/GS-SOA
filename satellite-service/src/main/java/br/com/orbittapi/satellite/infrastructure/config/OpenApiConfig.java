package br.com.orbittapi.satellite.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI satelliteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrbittAPI — Satellite Service")
                        .description("Bounded context: Satellite Data. Endpoints /landuse e /vegetation.")
                        .version("1.0.0"));
    }
}
