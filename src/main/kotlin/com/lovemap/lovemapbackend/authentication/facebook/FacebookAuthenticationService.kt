package com.lovemap.lovemapbackend.authentication.facebook

import com.lovemap.lovemapbackend.authentication.FacebookAuthenticationRequest
import com.lovemap.lovemapbackend.authentication.LoverAuthenticationResult
import com.lovemap.lovemapbackend.authentication.lover.LoverAuthenticationService
import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverConverter
import com.lovemap.lovemapbackend.lover.LoverService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.sql.Timestamp
import java.time.Instant

@Service
class FacebookAuthenticationService(
    private val jwtService: JwtService,
    private val loverService: LoverService,
    private val loverConverter: LoverConverter,
    private val authenticationManager: FacebookAuthenticationManager,
    private val loverAuthenticationService: LoverAuthenticationService,
) {

    suspend fun authenticate(request: FacebookAuthenticationRequest): LoverAuthenticationResult {
        val authenticationMono = authenticateWithFacebook(request)
        val lover = getOrCreateLover(request)
        return LoverAuthenticationResult(
            loverConverter.toResponse(lover),
            jwtService.generateToken(authenticationMono.awaitSingle())
        )
    }

    private fun authenticateWithFacebook(request: FacebookAuthenticationRequest): Mono<Authentication> {
        return authenticationManager.authenticate(
            FacebookAuthenticationToken(
                request.email,
                request.accessToken,
            )
        )
    }

    private suspend fun getOrCreateLover(
        request: FacebookAuthenticationRequest
    ): Lover {
        val lover = if (loverService.unAuthorizedExistsByEmail(request.email)) {
            loverService.unAuthorizedGetByEmail(request.email)
        } else {
            createLover(request.email)
        }
        return lover
    }

    suspend fun createLover(email: String): Lover {
        loverService.checkUserNameAndEmail(email, email)
        var lover = Lover(
            userName = email,
            email = email,
            createdAt = Timestamp.from(Instant.now())
        )
        lover = loverService.save(lover)
        loverAuthenticationService.createFacebookAuth(lover)
        return lover
    }
}