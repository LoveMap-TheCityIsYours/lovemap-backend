package com.smackmap.smackmapbackend.security

import io.jsonwebtoken.Claims
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import reactor.core.publisher.Mono

//@Component
class AuthenticationManager(
    private val jwtTokenUtil: JwtTokenUtil
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        val authToken = authentication!!.credentials.toString()
        val username: String = jwtTokenUtil.getUsernameFromToken(authToken)
        // TODO: migrate to jwtTokenUtil.validateTokenBetter
        return Mono.just(jwtTokenUtil.validateToken(authToken))
            .filter { valid -> valid }
            .switchIfEmpty(Mono.empty())
            .map {
                val claims: Claims = jwtTokenUtil.getAllClaimsFromToken(authToken)
                // TODO: maybe doesnt work
                val roles: List<String> = claims.get("role", List::class.java) as List<String>
                UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    roles.map { SimpleGrantedAuthority(it) }
                )
            }
    }
}