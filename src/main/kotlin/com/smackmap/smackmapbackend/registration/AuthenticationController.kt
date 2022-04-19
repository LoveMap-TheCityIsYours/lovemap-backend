package com.smackmap.smackmapbackend.registration

import com.smackmap.smackmapbackend.smacker.CreateSmackerRequest
import com.smackmap.smackmapbackend.smacker.LoginSmackerRequest
import com.smackmap.smackmapbackend.smacker.SmackerDto
import com.smackmap.smackmapbackend.smacker.SmackerRelationsDto
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
@RequestMapping("/registration")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/register")
    suspend fun register(@RequestBody request: CreateSmackerRequest): ResponseEntity<SmackerDto> {
        logger.debug { "Registering user '${request.userName}'" }
        val smacker = authenticationService.createSmacker(request)
        val jwt = authenticationService.generateToken(smacker.userName, request.password)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, jwt)
            .body(SmackerDto.of(smacker))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginSmackerRequest): ResponseEntity<SmackerRelationsDto> {
        validateLoginRequest(request)
        try {
            val smacker = authenticationService.loginSmacker(request)
            val jwt = authenticationService.generateToken(smacker.userName, request.password)
            return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(smacker)
        } catch (e: BadCredentialsException) {
            logger.debug(e) {
                "Login failed with username '${request.userName}', " +
                        "email ${request.email}."
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