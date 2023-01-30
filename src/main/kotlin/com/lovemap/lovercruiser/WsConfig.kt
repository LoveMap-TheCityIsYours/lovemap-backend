package com.lovemap.lovercruiser

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
    fun webSocketHandlerAdapter(): LoverCruiserWsHandlerAdapter {
        return LoverCruiserWsHandlerAdapter(jwtService)
    }

    @Bean
    fun handlerMapping(
        cachedLoverService: CachedLoverService,
        objectMapper: ObjectMapper
    ): HandlerMapping {
        return SimpleUrlHandlerMapping().apply {
            urlMap = mapOf(
                Pair(
                    "/lover-cruiser/chat",
                    ChatWebSocketHandler(jwtService, cachedLoverService, objectMapper)
                )
            )
        }
    }
}