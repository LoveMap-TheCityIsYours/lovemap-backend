package com.lovemap.loverlocator

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.lover.CachedLoverService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WsConfig(
    private val jwtService: JwtService
) {

    @Bean
    fun webSocketHandlerAdapter(): LoverLocatorWsHandlerAdapter {
        return LoverLocatorWsHandlerAdapter(jwtService)
    }

    @Bean
    fun handlerMapping(
        cachedLoverService: CachedLoverService,
        objectMapper: ObjectMapper
    ): HandlerMapping {
        return SimpleUrlHandlerMapping().apply {
            urlMap = mapOf(
                Pair(
                    "/lover-locator/chat",
                    ChatWebSocketHandler(jwtService, cachedLoverService, objectMapper)
                )
            )
        }
    }
}