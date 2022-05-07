package com.lovemap.lovemapbackend.authentication

import com.lovemap.lovemapbackend.security.JwtService
import com.lovemap.lovemapbackend.security.password.Password
import com.lovemap.lovemapbackend.security.password.PasswordService
import com.lovemap.lovemapbackend.lover.*
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
    private val loverService: LoverService,
    private val loverRelationService: LoverRelationService,
    private val passwordRepository: PasswordService,
    private val jwtService: JwtService,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun createLover(request: CreateLoverRequest): Lover {
        loverService.checkUserNameAndEmail(request.userName, request.email)
        var lover = Lover(
            userName = request.userName,
            email = request.email,
            createdAt = Timestamp.from(Instant.now())
        )
        lover = loverService.save(lover)
        val password = Password(
            passwordHash = passwordEncoder.encode(request.password),
            loverId = lover.id,
        )
        passwordRepository.save(password)
        return lover
    }

    suspend fun loginLover(request: LoginLoverRequest): LoverRelationsDto {
        logger.debug { "Logging in '$request'" }
        val lover = if (request.email != null) {
            loverService.unAuthorizedGetByEmail(request.email)
        } else {
            loverService.unAuthorizedGetByUserName(request.userName!!)
        }
        authenticateAndGetUser(lover.userName, request.password)
        return loverRelationService.getWithRelations(lover)
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