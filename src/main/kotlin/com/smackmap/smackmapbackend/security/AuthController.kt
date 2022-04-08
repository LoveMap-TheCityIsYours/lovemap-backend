package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.smacker.CreateSmackerRequest
import com.smackmap.smackmapbackend.smacker.LoginSmackerRequest
import com.smackmap.smackmapbackend.smacker.SmackerResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtTokenUtil: JwtTokenUtil,
) {
    @PostMapping("/register")
    fun register(@RequestBody createSmackerRequest: CreateSmackerRequest): ResponseEntity<SmackerResponse> {
        val smacker = authService.createSmacker(createSmackerRequest)
        return login(LoginSmackerRequest(smacker.userName, smacker.email, createSmackerRequest.password))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginSmackerRequest: LoginSmackerRequest): ResponseEntity<SmackerResponse> {
        if (loginSmackerRequest.email == null && loginSmackerRequest.userName == null) {
            return ResponseEntity.badRequest().build()
        }
        val (user, smacker) = authService.loginSmacker(loginSmackerRequest)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateToken(user))
            .body(SmackerResponse.of(smacker))
    }
}