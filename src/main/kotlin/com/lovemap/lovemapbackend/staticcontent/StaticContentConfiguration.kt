package com.lovemap.lovemapbackend.staticcontent

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Configuration
class StaticContentConfiguration {

    @Bean
    fun joinUsHtmlRouter(@Value("classpath:/public/join-us.html") html: Resource): RouterFunction<ServerResponse> {
        return route(
            GET("/join-us.html")
        ) {
            ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(html)
        }
    }

    @Bean
    fun joinUsLinkSharingRouter(@Value("classpath:/public/join-us.html") html: Resource): RouterFunction<ServerResponse> {
        return route(
            GET("/join-us/**")
        ) {
            ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(html)
        }
    }

    @Bean
    fun privacyPolicyHtmlRouter(@Value("classpath:/public/privacy-policy.html") html: Resource): RouterFunction<ServerResponse> {
        return route(
            GET("/privacy-policy.html")
        ) {
            ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(html)
        }
    }
}