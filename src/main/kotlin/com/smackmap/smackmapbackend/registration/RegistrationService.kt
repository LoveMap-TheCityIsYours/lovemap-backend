package com.smackmap.smackmapbackend.registration

import com.smackmap.smackmapbackend.security.password.Password
import com.smackmap.smackmapbackend.security.password.PasswordService
import com.smackmap.smackmapbackend.smacker.CreateSmackerRequest
import com.smackmap.smackmapbackend.smacker.LoginSmackerRequest
import com.smackmap.smackmapbackend.smacker.Smacker
import com.smackmap.smackmapbackend.smacker.SmackerService
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class RegistrationService(
    private val smackerService: SmackerService,
    private val passwordRepository: PasswordService,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun createSmacker(request: CreateSmackerRequest): Smacker {
        var smacker = Smacker(
            userName = request.userName,
            email = request.email,
        )
        smacker = smackerService.save(smacker)
        val password = Password(
            passwordHash = passwordEncoder.encode(request.password),
            smackerId = smacker.id
        )
        passwordRepository.save(password)
        return smacker
    }

    suspend fun loginSmacker(request: LoginSmackerRequest): Pair<Authentication, Smacker> {
        logger.debug { "Logging in '$request'" }
        val smacker = if (request.email != null) {
            smackerService.getByEmail(request.email)
        } else {
            smackerService.getByUserName(request.userName!!)
        }
        val authentication  = authenticateAndGetUser(smacker.userName, request.password)
        return Pair(authentication, smacker)
    }

    private suspend fun authenticateAndGetUser(userName: String, password: String): Authentication {
        val authenticationMono = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(userName, password))
        return authenticationMono.awaitSingle()
    }
}