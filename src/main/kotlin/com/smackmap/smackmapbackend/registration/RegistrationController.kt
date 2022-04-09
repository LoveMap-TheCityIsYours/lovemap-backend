package com.smackmap.smackmapbackend.registration

import com.smackmap.smackmapbackend.security.JwtService
import com.smackmap.smackmapbackend.smacker.CreateSmackerRequest
import com.smackmap.smackmapbackend.smacker.LoginSmackerRequest
import com.smackmap.smackmapbackend.smacker.SmackerResponse
import com.smackmap.smackmapbackend.smacker.SmackerService
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/registration")
class RegistrationController(
    private val registrationService: RegistrationService,
    private val jwtService: JwtService,

    private val smackerService: SmackerService
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping
    suspend fun test(): ResponseEntity<SmackerResponse> {
        return ResponseEntity.ok(SmackerResponse.of(smackerService.getById(1)))
    }

    @PostMapping("/register")
    suspend fun register(@RequestBody createSmackerRequest: CreateSmackerRequest): ResponseEntity<SmackerResponse> {
        logger.debug { "Registering user '${createSmackerRequest.userName}'" }
        val smacker = registrationService.createSmacker(createSmackerRequest)
        return login(LoginSmackerRequest(smacker.userName, smacker.email, createSmackerRequest.password))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody loginSmackerRequest: LoginSmackerRequest): ResponseEntity<SmackerResponse> {
        if (loginSmackerRequest.email.isNullOrEmpty() && loginSmackerRequest.userName.isNullOrEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Both email and username are missing from loginRequest.")
        }
        try {
            val (authentication, smacker) = registrationService.loginSmacker(loginSmackerRequest)
            logger.debug { "User login finished '$smacker'. Generating token." }
            val jwtToken = jwtService.generateToken(authentication)
            logger.debug { "Jwt token generated for '$smacker'." }
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .body(SmackerResponse.of(smacker))
        } catch (e: BadCredentialsException) {
            logger.debug(e) { "Login failed with username '${loginSmackerRequest.userName}', " +
                    "email ${loginSmackerRequest.email}." }
            throw ResponseStatusException(HttpStatus.FORBIDDEN, e.message)
        }
    }
}