package com.lovemap.lovemapbackend.authentication.facebook

import com.lovemap.lovemapbackend.authentication.FacebookAuthenticationRequest
import com.lovemap.lovemapbackend.authentication.LoverAuthenticationResult
import com.lovemap.lovemapbackend.authentication.lover.LoverAuthentication
import com.lovemap.lovemapbackend.authentication.lover.LoverAuthenticationService
import com.lovemap.lovemapbackend.authentication.security.JwtService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverConverter
import com.lovemap.lovemapbackend.lover.LoverService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
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
        val loverAuthentication = loverAuthenticationService.findByFacebookId(request.facebookId)
        return if (loverAuthentication != null) {
            loginRegisteredFacebookUser(request, loverAuthentication)
        } else {
            registerNewFacebookUser(request)
        }
    }

    private suspend fun loginRegisteredFacebookUser(
        request: FacebookAuthenticationRequest,
        loverAuthentication: LoverAuthentication,
    ): LoverAuthenticationResult {
        // user already registered with facebook.
        val lover = loverService.unAuthorizedGetById(loverAuthentication.id)
        val token = facebookAuthenticationToken(lover, request)
        return authenticateAndLogin(lover, token)
    }

    private fun facebookAuthenticationToken(
        lover: Lover,
        request: FacebookAuthenticationRequest
    ): FacebookAuthenticationToken {
        return FacebookAuthenticationToken(
            email = lover.email,
            userName = lover.userName,
            facebookId = request.facebookId,
            fbAccessToken = request.accessToken,
        )
    }

    private suspend fun authenticateAndLogin(
        lover: Lover,
        token: FacebookAuthenticationToken
    ): LoverAuthenticationResult {
        val authentication = authenticateWithFacebook(token)
        return LoverAuthenticationResult(
            loverConverter.toResponse(lover),
            jwtService.generateToken(authentication)
        )
    }

    private suspend fun authenticateWithFacebook(token: FacebookAuthenticationToken): Authentication {
        return authenticationManager.authenticate(token).doOnError { throw it }.awaitSingle()
    }

    private suspend fun registerNewFacebookUser(
        request: FacebookAuthenticationRequest
    ): LoverAuthenticationResult {
        // user did not register with facebook yet
        // checking whether the email is already registered:
        val lover = if (loverService.unAuthorizedExistsByEmail(request.email)) {
            connectExistingLoverWithFacebook(request)
        } else {
            // new user registration
            createNewLover(request.email, request.facebookId)
        }
        val token = facebookAuthenticationToken(lover, request)
        return authenticateAndLogin(lover, token)
    }

    private suspend fun connectExistingLoverWithFacebook(
        request: FacebookAuthenticationRequest,
    ): Lover {
        val existingLover = loverService.unAuthorizedGetByEmail(request.email)
        authenticateWithFacebook(facebookAuthenticationToken(existingLover, request))
        val loverAuthentication = loverAuthenticationService.getLoverAuthentication(existingLover)
        loverAuthentication.facebookId = request.facebookId
        loverAuthenticationService.save(loverAuthentication)
        return existingLover
    }

    suspend fun createNewLover(email: String, facebookId: String): Lover {
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