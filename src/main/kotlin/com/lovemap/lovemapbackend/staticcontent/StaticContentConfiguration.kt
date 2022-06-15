package com.lovemap.lovemapbackend.staticcontent

import com.lovemap.lovemapbackend.email.EmailService
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
import org.springframework.web.reactive.function.server.coRouter
import java.lang.annotation.RetentionPolicy

@Configuration
class StaticContentConfiguration(
    @Value("classpath:/public/favicon.ico") private val favicon: Resource,
    @Value("classpath:/public/join-us.html") private val joinUsHtml: Resource,
    @Value("classpath:/public/privacy-policy.html") private val privacyPolicyHtml: Resource,
    private val emailService: EmailService
) {

    @Bean
    fun mainRouter() = coRouter {
        GET("/favicon.ico") {
            ok().contentType(MediaType.IMAGE_PNG)
                .bodyValue(favicon).block()!!
        }
        GET("/join-us.html") {
            emailService.sendEmail()
            ok().contentType(MediaType.TEXT_HTML)
                .bodyValue(joinUsHtml).block()!!
        }
        GET("/join-us/**") {
            ok().contentType(MediaType.TEXT_HTML)
                .bodyValue(joinUsHtml).block()!!
        }
        GET("/privacy-policy.html") {
            ok().contentType(MediaType.TEXT_HTML)
                .bodyValue(privacyPolicyHtml).block()!!
        }
        GET("/.well-known/assetlinks.json") {
            ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    """
                    [{
                      "relation": ["delegate_permission/common.handle_all_urls"],
                      "target" : { "namespace": "android_app", "package_name": "com.lovemap.lovemapandroid",
                                   "sha256_cert_fingerprints": ["C4:97:10:B3:46:38:45:65:67:41:88:8C:F1:00:D0:DD:20:ED:87:82:A5:54:44:30:8A:53:15:F7:16:47:ED:54"] }
                    }]
                """.trimIndent()
                ).block()!!
        }
    }
}