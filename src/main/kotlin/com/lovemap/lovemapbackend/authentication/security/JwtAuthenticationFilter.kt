package com.lovemap.lovemapbackend.authentication.security

import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val lovemapClients: LoveMapClients,
    private val environment: Environment,
) : WebFilter {
    private val logger = KotlinLogging.logger {}

    private val clientSecretExclusions = listOf(
        "/lover-cruiser",
        "/actuator/info",
        "/actuator/health",
        "/actuator/metrics",
        "/actuator/prometheus",
        "/privacy-policy.html",
        "/terms-of-use.html",
        "/.well-known/assetlinks.json",
        "/join-us",
        "/favicon.ico",
        "/webjars",
        "/swagger",
        "/v3/api-docs",
        "/debug",
        "/app-ads.txt"
    )

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return if (excludedFromClientSecretFilter(exchange)) {
            tokenFilter(exchange, chain)
        } else {
            if (containsValidClientSecret(exchange)) {
                tokenFilter(exchange, chain)
            } else {
                logger.warn { "Denying request from '${exchange.request.remoteAddress}' to '${exchange.request.path}'" }
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                Mono.empty()
            }
        }
    }

    private fun excludedFromClientSecretFilter(exchange: ServerWebExchange) =
        clientSecretExclusions.any { exclusion -> exchange.request.path.toString().startsWith(exclusion) }
                || environment.activeProfiles.contains("dev")

    private fun containsValidClientSecret(exchange: ServerWebExchange): Boolean {
        val clientId = exchange.request.headers.getFirst("x-client-id")
        val clientSecret = exchange.request.headers.getFirst("x-client-secret")
        clientId?.let { id ->
            clientSecret?.let { secret ->
                if (lovemapClients.contains(id, secret)) {
                    return true
                }
            }
        }
        return false
    }

    private fun tokenFilter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val token: String? = jwtService.resolveToken(exchange.request)
        if (token != null && jwtService.validateToken(token)) {
            val authentication: Authentication = jwtService.getAuthentication(token)
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }
        return chain.filter(exchange)
    }
}

@ConfigurationProperties(prefix = "lovemap")
data class LoveMapClients @ConstructorBinding constructor(val clients: List<Client>) {
    data class Client(
        val id: String,
        val secret: String
    )

    fun contains(clientId: String, clientSecret: String): Boolean {
        return clients.any { it.id == clientId && it.secret == clientSecret }
    }
}
