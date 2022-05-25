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

    @Bean
    fun assetLinksJson(): RouterFunction<ServerResponse> {
        return route(
            GET("/.well-known/assetlinks.json")
        ) {
            ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    [{
                      "relation": ["delegate_permission/common.handle_all_urls"],
                      "target" : { "namespace": "android_app", "package_name": "com.lovemap.lovemapandroid",
                                   "sha256_cert_fingerprints": ["C4:97:10:B3:46:38:45:65:67:41:88:8C:F1:00:D0:DD:20:ED:87:82:A5:54:44:30:8A:53:15:F7:16:47:ED:54"] }
                    }]
                """.trimIndent())
        }
    }
}