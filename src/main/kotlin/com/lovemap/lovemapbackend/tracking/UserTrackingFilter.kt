package com.lovemap.lovemapbackend.tracking

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import kotlinx.coroutines.reactor.mono
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
class UserTrackingFilter(
    private val authorizationService: AuthorizationService,
    private val userTrackingService: UserTrackingService
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestPath = exchange.request.path.toString()
        return ReactiveSecurityContextHolder.getContext().doOnNext {
            if (it.authentication != null) {
                mono {
                    authorizationService.getLoverFromContext(it)?.let { caller ->
                        userTrackingService.trackUser(caller, requestPath)
                    }
                }.subscribe()
            }
        }.then(chain.filter(exchange))
    }
}
