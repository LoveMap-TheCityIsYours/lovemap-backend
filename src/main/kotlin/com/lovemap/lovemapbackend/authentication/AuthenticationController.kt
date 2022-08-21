package com.lovemap.lovemapbackend.authentication

import com.lovemap.lovemapbackend.authentication.facebook.FacebookAuthenticationService
import com.lovemap.lovemapbackend.authentication.password.PasswordAuthenticationService
import com.lovemap.lovemapbackend.authentication.password.PasswordResetService
import com.lovemap.lovemapbackend.lover.LoverConverter
import com.lovemap.lovemapbackend.lover.LoverRelationsResponse
import com.lovemap.lovemapbackend.lover.LoverResponse
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import com.lovemap.lovemapbackend.utils.ValidatorService
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authentication")
class AuthenticationController(
    private val passwordAuthenticationService: PasswordAuthenticationService,
    private val facebookAuthenticationService: FacebookAuthenticationService,
    private val passwordResetService: PasswordResetService,
    private val validatorService: ValidatorService,
    private val loverConverter: LoverConverter,
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/register")
    suspend fun register(@RequestBody request: CreateLoverRequest): ResponseEntity<LoverResponse> {
        validatorService.validate(request)
        logger.debug { "Registering user '${request.userName}'" }
        val lover = passwordAuthenticationService.createLover(request)
        val jwt = passwordAuthenticationService.generateToken(lover.userName, request.password)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .body(loverConverter.toResponse(lover))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginLoverRequest): ResponseEntity<LoverRelationsResponse> {
        validateLoginRequest(request)
        try {
            val lover = passwordAuthenticationService.loginLover(request)
            val jwt = passwordAuthenticationService.generateToken(lover.userName, request.password)
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(lover)
        } catch (e: BadCredentialsException) {
            logger.debug(e) {
                "Login failed with username '${request.userName}', email ${request.email}."
            }
            throw LoveMapException(
                HttpStatus.FORBIDDEN, ErrorMessage(
                    ErrorCode.InvalidCredentials,
                    "Username: '${request.userName}', email: '${request.email}'",
                    "Login failed."
                )
            )
        }
    }

    @PostMapping("/request-password-reset")
    suspend fun requestPasswordReset(@RequestBody request: ResetPasswordRequest): ResponseEntity<ResetPasswordResponse> {
        validatorService.validate(request)
        passwordResetService.initPasswordReset(request)
        return ResponseEntity.ok(ResetPasswordResponse("Instructions sent in email."))
    }

    @PostMapping("/new-password")
    suspend fun newPassword(@RequestBody request: NewPasswordRequest): ResponseEntity<LoverRelationsResponse> {
        validatorService.validate(request)
        val lover: LoverRelationsResponse = passwordResetService.setNewPassword(request)
        val jwt = passwordAuthenticationService.generateToken(lover.userName, request.newPassword)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .body(lover)
    }

    @PostMapping("/facebook-login")
    suspend fun facebookLogin(@RequestBody request: FacebookAuthenticationRequest): ResponseEntity<LoverResponse> {
        validatorService.validate(request)
        val loverAuthenticationResult = facebookAuthenticationService.authenticate(request)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, loverAuthenticationResult.jwt)
            .body(loverAuthenticationResult.loverResponse)
    }

    private fun validateLoginRequest(request: LoginLoverRequest) {
        validatorService.validate(request)
        if (request.email.isNullOrEmpty() && request.userName.isNullOrEmpty()) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.InvalidCredentials,
                    "Username: '${request.userName}', email: '${request.email}'",
                    "Both email and username are missing from loginRequest."
                )
            )
        }
    }
}