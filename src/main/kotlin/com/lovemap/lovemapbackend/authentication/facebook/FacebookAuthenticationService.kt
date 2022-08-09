package com.lovemap.lovemapbackend.authentication.facebook

import com.lovemap.lovemapbackend.authentication.FacebookAuthenticationRequest
import com.lovemap.lovemapbackend.authentication.LoverAuthenticationResult
import com.lovemap.lovemapbackend.authentication.lover.LoverAuthentication
import com.lovemap.lovemapbackend.authentication.lover.LoverAuthenticationService
import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverConverter
import com.lovemap.lovemapbackend.lover.LoverService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
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
        val authentication = authenticationMono.doOnError { throw it }.awaitSingle()
        val loverAuthentication = loverAuthenticationService.findByFacebookId(request.facebookId)
        return if (loverAuthentication != null) {
            loginRegisteredUser(loverAuthentication, authenticationMono)
        } else {
            registerNewUser(request, authentication)
        }
    }

    private fun authenticateWithFacebook(request: FacebookAuthenticationRequest): Mono<Authentication> {
        return authenticationManager.authenticate(
            FacebookAuthenticationToken(
                email = request.email,
                facebookId = request.facebookId,
                fbAccessToken = request.accessToken,
            )
        )
    }

    private suspend fun loginRegisteredUser(
        loverAuthentication: LoverAuthentication,
        authenticationMono: Mono<Authentication>
    ): LoverAuthenticationResult {
        val lover = loverService.unAuthorizedGetById(loverAuthentication.id)
        // user already registered with facebook.
        return LoverAuthenticationResult(
            loverConverter.toResponse(lover),
            jwtService.generateToken(authenticationMono.awaitSingle())
        )
    }

    private suspend fun registerNewUser(
        request: FacebookAuthenticationRequest,
        authentication: Authentication
    ): LoverAuthenticationResult {
        // user did not register with facebook yet
        // checking whether the email is already registered:
        if (loverService.unAuthorizedExistsByEmail(request.email)) {
            // todo: implement api for connecting existing user with facebook account
            throw LoveMapException(HttpStatus.CONFLICT, ErrorCode.FacebookEmailOccupied)
        } else {
            // new user registration
            val lover = createLover(request.email, request.facebookId)
            return LoverAuthenticationResult(
                loverConverter.toResponse(lover),
                jwtService.generateToken(authentication)
            )
        }
    }

    suspend fun createLover(email: String, facebookId: String): Lover {
        loverService.checkUserNameAndEmail(email, email)
        var lover = Lover(
            userName = email,
            email = email,
            createdAt = Timestamp.from(Instant.now())
        )
        lover = loverService.save(lover)
        loverAuthenticationService.createFacebookAuth(lover, facebookId)
        return lover
    }
}