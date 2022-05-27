package com.lovemap.lovemapbackend.authentication

import com.lovemap.lovemapbackend.lover.*
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
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/authentication")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val validatorService: ValidatorService,
    private val loverConverter: LoverConverter,
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/register")
    suspend fun register(@RequestBody request: CreateLoverRequest): ResponseEntity<LoverDto> {
        validatorService.validate(request)
        logger.debug { "Registering user '${request.userName}'" }
        val lover = authenticationService.createLover(request)
        val jwt = authenticationService.generateToken(lover.userName, request.password)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .body(loverConverter.toDto(lover))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginLoverRequest): ResponseEntity<LoverRelationsDto> {
        validateLoginRequest(request)
        try {
            val lover = authenticationService.loginLover(request)
            val jwt = authenticationService.generateToken(lover.userName, request.password)
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(lover)
        } catch (e: BadCredentialsException) {
            logger.debug(e) {
                "Login failed with username '${request.userName}', " +
                        "email ${request.email}."
            }
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
        }
    }

    private fun validateLoginRequest(request: LoginLoverRequest) {
        validatorService.validate(request)
        if (request.email.isNullOrEmpty() && request.userName.isNullOrEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Both email and username are missing from loginRequest."
            )
        }
    }
}