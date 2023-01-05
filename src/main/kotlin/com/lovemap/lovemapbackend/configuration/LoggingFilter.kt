package com.lovemap.lovemapbackend.configuration

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class LoggingFilter(val requestLogger: RequestLogger) : WebFilter {
    private val logger = KotlinLogging.logger {}

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        logger.info(requestLogger.getRequestMessage(exchange))
        val filter = chain.filter(exchange)
        exchange.response.beforeCommit {
            logger.info(requestLogger.getResponseMessage(exchange))
            Mono.empty()
        }
        return filter
    }
}

@Component
class RequestLogger {

    fun getRequestMessage(exchange: ServerWebExchange): String {
        val request = exchange.request
        val method = request.method
        val path = request.uri.path
        return ">>> [$method] [$path] ${request.headers}"
    }

    fun getResponseMessage(exchange: ServerWebExchange): String {
        val request = exchange.request
        val response = exchange.response
        val method = request.method
        val path = request.uri.path
        val statusCode = getStatus(response)
        return "<<< [$statusCode] [$method] [$path] ${request.headers}"
    }

    private fun getStatus(response: ServerHttpResponse): HttpStatusCode = try {
        response.statusCode!!
    } catch (ex: Exception) {
        HttpStatus.CONTINUE
    }
}