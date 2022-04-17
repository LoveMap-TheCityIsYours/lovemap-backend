package com.smackmap.smackmapbackend.registration

import com.smackmap.smackmapbackend.security.JwtService
import com.smackmap.smackmapbackend.smacker.*
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/registration")
class RegistrationController(
    private val registrationService: RegistrationService,
    private val jwtService: JwtService,
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/register")
    suspend fun register(@RequestBody createSmackerRequest: CreateSmackerRequest): ResponseEntity<SmackerRelationsDto> {
        logger.debug { "Registering user '${createSmackerRequest.userName}'" }
        val smacker = registrationService.createSmacker(createSmackerRequest)
        return login(LoginSmackerRequest(smacker.userName, smacker.email, createSmackerRequest.password))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody loginSmackerRequest: LoginSmackerRequest): ResponseEntity<SmackerRelationsDto> {
        validateLoginRequest(loginSmackerRequest)
        try {
            val (authentication, smacker) = registrationService.loginSmacker(loginSmackerRequest)
            logger.debug { "User login finished '$smacker'. Generating token." }
            val jwtToken = jwtService.generateToken(authentication)
            logger.debug { "Jwt token generated for '$smacker'." }
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .body(smacker)
        } catch (e: BadCredentialsException) {
            logger.debug(e) {
                "Login failed with username '${loginSmackerRequest.userName}', " +
                        "email ${loginSmackerRequest.email}."
            }
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
        }
    }

    private fun validateLoginRequest(loginSmackerRequest: LoginSmackerRequest) {
        if (loginSmackerRequest.email.isNullOrEmpty() && loginSmackerRequest.userName.isNullOrEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Both email and username are missing from loginRequest."
            )
        }
    }
}