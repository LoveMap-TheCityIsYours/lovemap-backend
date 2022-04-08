package com.smackmap.smackmapbackend.smacker

import com.smackmap.smackmapbackend.security.JwtTokenUtil
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.math.log

@RestController
@RequestMapping("/smacker")
class SmackerController(
    private val smackerService: SmackerService,
    private val jwtTokenUtil: JwtTokenUtil,
) {

    @PostMapping("/register")
    fun register(@RequestBody createSmackerRequest: CreateSmackerRequest): ResponseEntity<SmackerResponse> {
        val (smacker, password) = smackerService.createSmacker(createSmackerRequest)
        return login(LoginSmackerRequest(smacker.userName, smacker.email, password.passwordHash))
    }

    @PostMapping("/login")
    fun login(@RequestBody loginSmackerRequest: LoginSmackerRequest): ResponseEntity<SmackerResponse> {
        if (loginSmackerRequest.email == null && loginSmackerRequest.userName == null) {
            return ResponseEntity.badRequest().build()
        }
        val (user, smacker) = smackerService.loginSmacker(loginSmackerRequest)
        return ResponseEntity.ok()
            .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateToken(user))
            .body(SmackerResponse.of(smacker))
    }

    @GetMapping("/{smackerId}")
    fun getSmacker(@PathVariable smackerId: Long): ResponseEntity<SmackerResponse> {
        TODO("Not yet implemented")
    }

    @PostMapping("/generateLink")
    fun generateSmackerLink(@RequestBody generateSmackerLinkRequest: GenerateSmackerLinkRequest)
            : ResponseEntity<SmackerLinkResponse> {

        TODO("Not yet implemented")
    }

    @GetMapping("/byLink")
    fun getSmackerByLink(@RequestBody getSmackerByLinkRequest: GetSmackerByLinkRequest)
            : ResponseEntity<SmackerResponse> {

        TODO("Not yet implemented")
    }

    @PostMapping("/requestPartnership")
    fun requestPartnership(@RequestBody requestPartnershipRequest: RequestPartnershipRequest)
            : ResponseEntity<SmackerResponse> {

        TODO("Not yet implemented")
    }

    @PostMapping("/acceptPartnership")
    fun acceptPartnership(@RequestBody acceptPartnershipRequest: AcceptPartnershipRequest)
            : ResponseEntity<SmackerResponse> {

        TODO("Not yet implemented")
    }
}