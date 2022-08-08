package com.lovemap.lovemapbackend.authentication.facebook

import com.lovemap.lovemapbackend.authentication.security.AUTHORITY_ADMIN
import com.lovemap.lovemapbackend.authentication.security.AUTHORITY_USER
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import reactor.core.publisher.Mono

class FacebookAuthenticationManager(
    @Value("\${lovemap.admins.emails}")
    private val adminEmails: List<String>,
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return if (authentication is FacebookAuthenticationToken) {
            mono {
                // todo: FB api call: https://developers.facebook.com/docs/facebook-login/guides/advanced/manual-flow/#checktoken
                val email = authentication.principal as String
                FacebookAuthenticationToken(
                    email = email,
                    fbAccessToken = authentication.credentials as String,
                    isAuthenticated = true,
                    authorities = getGrantedAuthorities(email)
                )
            }
        } else {
            Mono.empty()
        }
    }

    private fun getGrantedAuthorities(email: String): MutableList<SimpleGrantedAuthority> {
        val authorities = ArrayList<String>()
        authorities.add(AUTHORITY_USER)
        if (adminEmails.contains(email)) {
            authorities.add(AUTHORITY_ADMIN)
        }
        return authorities.map { SimpleGrantedAuthority(it) }.toMutableList()
    }
}