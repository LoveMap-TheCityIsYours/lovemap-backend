package com.lovemap.lovemapbackend.staticcontent

import com.lovemap.lovemapbackend.email.EmailService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class StaticContentConfiguration(
    @Value("classpath:/public/favicon.ico") private val favicon: Resource,
    @Value("classpath:/public/join-us.html") private val joinUsHtml: Resource,
    @Value("classpath:/public/privacy-policy.html") private val privacyPolicyHtml: Resource,
    @Value("classpath:/public/delete-profile.html") private val deleteProfileHtml: Resource,
    @Value("classpath:/public/terms-of-use.html") private val termsOfUseHtml: Resource,
    @Value("classpath:/public/app-ads.txt") private val appAdsTxt: Resource,
    private val emailService: EmailService,
) {

    @Bean
    fun mainRouter() = coRouter {
        GET("/favicon.ico") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.IMAGE_PNG)
                    .bodyValue(favicon).awaitSingle()
            }
        }
        GET("/join-us.html") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.TEXT_HTML)
                    .bodyValue(joinUsHtml).awaitSingle()
            }
        }
        GET("/join-us/**") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.TEXT_HTML)
                    .bodyValue(joinUsHtml).awaitSingle()
            }
        }
        GET("/privacy-policy.html") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.TEXT_HTML)
                    .bodyValue(privacyPolicyHtml).awaitSingle()
            }
        }
        GET("/delete-profile.html") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.TEXT_HTML)
                    .bodyValue(deleteProfileHtml).awaitSingle()
            }
        }
        GET("/terms-of-use.html") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.TEXT_HTML)
                    .bodyValue(termsOfUseHtml).awaitSingle()
            }
        }
        GET("/app-ads.txt") {
            withContext(Dispatchers.IO) {
                ok().contentType(MediaType.TEXT_HTML)
                    .bodyValue(appAdsTxt).awaitSingle()
            }
        }
        GET("/.well-known/assetlinks.json") {
            withContext(Dispatchers.IO) {
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
                    ).awaitSingle()
            }
        }
    }
}