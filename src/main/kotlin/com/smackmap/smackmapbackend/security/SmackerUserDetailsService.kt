package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.security.password.PasswordService
import com.smackmap.smackmapbackend.smacker.SmackerService
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SmackerUserDetailsService(
    private val smackerService: SmackerService,
    private val passwordService: PasswordService,
    private val passwordEncoder: PasswordEncoder
) : ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
    private val logger = KotlinLogging.logger {}

    override fun findByUsername(username: String): Mono<UserDetails> {
        logger.debug { "Finding by username '$username'" }
        val smackerMono = mono { smackerService.getByUserName(username) }
        val userDetails: Mono<Mono<UserDetails>> = smackerMono.map { smacker ->
            val passwordMono = mono { passwordService.getPasswordOfSmacker(smacker) }
            passwordMono.map { password ->
                SmackerUserDetails.of(smacker, password)
            }
        }
        return userDetails.flatMap { it }
    }

    override fun updatePassword(user: UserDetails, newPassword: String): Mono<UserDetails> {
        logger.debug { "Updating password for user '$user'" }
        val smackerMono = mono { smackerService.getByUserName(user.username) }
        smackerMono.map { smacker ->
            val passwordMono = mono { passwordService.getPasswordOfSmacker(smacker) }
            passwordMono.map {
                it.passwordHash = passwordEncoder.encode(newPassword)
                mono { passwordService.save(it) }
            }
        }
        return Mono.just(user)
    }
}