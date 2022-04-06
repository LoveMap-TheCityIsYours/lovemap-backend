package com.smackmap.smackmapbackend.configuration

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ApiDocConfiguration {

    @Bean
    fun smackmapOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("Smackmap API")
                    .description("Smackmap API")
                    .version("v0.0.1")
            )
    }
}