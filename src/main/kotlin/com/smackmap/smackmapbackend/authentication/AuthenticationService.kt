package com.smackmap.smackmapbackend.authentication

import com.smackmap.smackmapbackend.security.JwtService
import com.smackmap.smackmapbackend.security.password.Password
import com.smackmap.smackmapbackend.security.password.PasswordService
import com.smackmap.smackmapbackend.smacker.*
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class AuthenticationService(
    private val smackerService: SmackerService,
    private val smackerRelationService: SmackerRelationService,
    private val passwordRepository: PasswordService,
    private val jwtService: JwtService,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun createSmacker(request: CreateSmackerRequest): Smacker {
        smackerService.checkUserNameAndEmail(request.userName, request.email)
        var smacker = Smacker(
            userName = request.userName,
            email = request.email,
            createdAt = Timestamp.from(Instant.now())
        )
        smacker = smackerService.save(smacker)
        val password = Password(
            passwordHash = passwordEncoder.encode(request.password),
            smackerId = smacker.id,
        )
        passwordRepository.save(password)
        return smacker
    }

    suspend fun loginSmacker(request: LoginSmackerRequest): SmackerRelationsDto {
        logger.debug { "Logging in '$request'" }
        val smacker = if (request.email != null) {
            smackerService.unAuthorizedGetByEmail(request.email)
        } else {
            smackerService.unAuthorizedGetByUserName(request.userName!!)
        }
        authenticateAndGetUser(smacker.userName, request.password)
        return smackerRelationService.getWithRelations(smacker)
    }

    suspend fun generateToken(userName: String, password: String): String {
        logger.debug { "User login finished '$userName'. Generating token." }
        val authentication = authenticateAndGetUser(userName, password)
        return jwtService.generateToken(authentication)
    }

    private suspend fun authenticateAndGetUser(userName: String, password: String): Authentication {
        val authenticationMono = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(userName, password))
        return authenticationMono.awaitSingle()
    }
}