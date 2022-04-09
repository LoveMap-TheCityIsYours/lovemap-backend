package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.security.password.PasswordService
import com.smackmap.smackmapbackend.smacker.SmackerService
import kotlinx.coroutines.runBlocking
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

    override fun findByUsername(username: String): Mono<UserDetails> {
        val smacker = runBlocking {
            smackerService.getByUserName(username)
        }
        val password = runBlocking {
            passwordService.getPasswordOfSmacker(smacker)
        }
        return Mono.just(SmackerUserDetails.of(smacker, password))
    }

    override fun updatePassword(user: UserDetails, newPassword: String): Mono<UserDetails> {
        val smacker = runBlocking { smackerService.getByUserName(user.username) }
        runBlocking {
            val password = passwordService.getPasswordOfSmacker(smacker)
            password.passwordHash = passwordEncoder.encode(newPassword)
            passwordService.save(password)
        }
        return Mono.just(user)
    }
}