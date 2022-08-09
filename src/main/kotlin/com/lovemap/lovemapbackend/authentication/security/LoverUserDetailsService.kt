package com.lovemap.lovemapbackend.authentication.security

import com.lovemap.lovemapbackend.authentication.lover.LoverAuthenticationService
import com.lovemap.lovemapbackend.lover.LoverService
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

const val AUTHORITY_USER = "ROLE_USER"
const val AUTHORITY_ADMIN = "ROLE_ADMIN"

@Service
class LoverUserDetailsService(
    private val loverService: LoverService,
    private val loverAuthenticationService: LoverAuthenticationService,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${lovemap.admins.emails}")
    private val adminEmails: List<String>,
) : ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
    private val logger = KotlinLogging.logger {}

    override fun findByUsername(username: String): Mono<UserDetails> {
        logger.debug { "Finding by username '$username'" }
        val loverMono = mono { loverService.unAuthorizedGetByUserName(username) }
        val userDetails: Mono<Mono<UserDetails>> = loverMono.map { lover ->
            val loverAuthenticationMono = mono { loverAuthenticationService.getLoverAuthentication(lover) }
            val userDetailsMono: Mono<UserDetails> = loverAuthenticationMono.map { loverAuthentication ->
                val authorities = ArrayList<String>()
                authorities.add(AUTHORITY_USER)
                if (adminEmails.contains(lover.email)) {
                    authorities.add(AUTHORITY_ADMIN)
                }
                LoverUserDetails.of(lover, loverAuthentication, authorities)
            }
            userDetailsMono
        }
        return userDetails.flatMap { it }
    }

    override fun updatePassword(user: UserDetails, newPassword: String): Mono<UserDetails> {
        logger.debug { "Updating password for user '$user'" }
        val loverMono = mono { loverService.unAuthorizedGetByUserName(user.username) }
        loverMono.map { lover ->
            val passwordMono = mono { loverAuthenticationService.getLoverAuthentication(lover) }
            passwordMono.map {
                it.passwordHash = passwordEncoder.encode(newPassword)
                mono { loverAuthenticationService.save(it) }
            }
        }
        return Mono.just(user)
    }
}