package com.hotresvib.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI / Swagger configuration
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("HotResvib API")
                    .version("v1")
                    .description("HotResvib backend API documentation")
                    .contact(Contact().name("HotResvib Support").email("support@hotresvib.com"))
                    .license(License().name("MIT").url("https://opensource.org/licenses/MIT"))
            )
    }
}
