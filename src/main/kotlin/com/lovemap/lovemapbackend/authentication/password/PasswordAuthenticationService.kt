package com.lovemap.lovemapbackend.authentication.password

import com.lovemap.lovemapbackend.authentication.CreateLoverRequest
import com.lovemap.lovemapbackend.authentication.LoginLoverRequest
import com.lovemap.lovemapbackend.authentication.lover.LoverAuthenticationService
import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.geolocation.GeoLocation.Companion.GLOBAL_LOCATION
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverRelationService
import com.lovemap.lovemapbackend.lover.LoverRelationsResponse
import com.lovemap.lovemapbackend.lover.LoverService
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class PasswordAuthenticationService(
    private val loverService: LoverService,
    private val loverRelationService: LoverRelationService,
    private val loverAuthenticationService: LoverAuthenticationService,
    private val jwtService: JwtService,
    private val userDetailsRepositoryReactiveAuthenticationManager: UserDetailsRepositoryReactiveAuthenticationManager,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun createLover(request: CreateLoverRequest): Lover {
        loverService.checkUserNameAndEmail(request.userName, request.email)
        var lover = Lover(
            userName = request.userName,
            email = request.email,
            displayName = loverAuthenticationService.getDisplayName(request.userName, request.email),
            registrationCountry = loverAuthenticationService.getRegistrationCountry(request.registrationCountry),
            publicProfile = request.publicProfile,
            createdAt = Timestamp.from(Instant.now())
        )
        lover = loverService.save(lover)
        loverAuthenticationService.createPassword(lover, request.password)
        return lover
    }

    suspend fun loginLover(request: LoginLoverRequest): LoverRelationsResponse {
        logger.debug { "Logging in '$request'" }
        val lover = if (request.email != null) {
            loverService.unAuthorizedGetByEmail(request.email)
        } else {
            loverService.unAuthorizedGetByUserName(request.userName!!)
        }
        authenticateUser(lover.userName, request.password)
        return loverRelationService.getWithRelations(lover)
    }

    suspend fun generateToken(userName: String, password: String): String {
        logger.debug { "User login finished '$userName'. Generating token." }
        val authentication = authenticateUser(userName, password)
        return jwtService.generateToken(authentication)
    }

    private suspend fun authenticateUser(userName: String, password: String): Authentication {
        val authenticationMono = userDetailsRepositoryReactiveAuthenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(userName, password))
        return authenticationMono.awaitSingle()
    }
}