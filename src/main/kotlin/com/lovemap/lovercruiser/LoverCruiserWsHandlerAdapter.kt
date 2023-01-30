package com.lovemap.lovercruiser

import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class LoverCruiserWsHandlerAdapter(
    private val jwtService: JwtService
) : WebSocketHandlerAdapter() {

    override fun handle(exchange: ServerWebExchange, handler: Any): Mono<HandlerResult> {
        val webSocketHandler = handler as WebSocketHandler
        return exchange.request.headers["Authorization"]?.firstOrNull()?.let {
            if (jwtService.validateToken(it)) {
                webSocketService.handleRequest(exchange, webSocketHandler).then(Mono.empty())
            } else {
                Mono.error(LoveMapException(HttpStatus.UNAUTHORIZED, ErrorCode.Forbidden))
            }
        } ?: Mono.error(LoveMapException(HttpStatus.UNAUTHORIZED, ErrorCode.Forbidden))
    }
}