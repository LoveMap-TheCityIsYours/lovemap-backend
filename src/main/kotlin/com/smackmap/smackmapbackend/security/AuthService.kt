package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.security.password.Password
import com.smackmap.smackmapbackend.security.password.PasswordService
import com.smackmap.smackmapbackend.smacker.CreateSmackerRequest
import com.smackmap.smackmapbackend.smacker.LoginSmackerRequest
import com.smackmap.smackmapbackend.smacker.Smacker
import com.smackmap.smackmapbackend.smacker.SmackerService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val smackerService: SmackerService,
    private val passwordRepository: PasswordService,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {

    fun createSmacker(request: CreateSmackerRequest): Smacker {
        var smacker = Smacker(
            userName = request.userName,
            email = request.email,
            partner = null
        )
        smacker = smackerService.save(smacker)
        val password = Password(
            passwordHash = passwordEncoder.encode(request.password),
            smacker = smacker
        )
        passwordRepository.save(password)
        return smacker
    }

    fun loginSmacker(request: LoginSmackerRequest): Pair<UserDetails, Smacker> {
        val smacker = if (request.email != null) {
            smackerService.getByEmail(request.email)
        } else {
            smackerService.getByUserName(request.userName!!)
        }
        val user: UserDetails = authenticateAndGetUser(smacker.userName, request.password)
        return Pair(user, smacker)
    }

    private fun authenticateAndGetUser(userName: String, password: String): UserDetails {
        val authentication: Authentication = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(userName, password))
        return authentication.principal as UserDetails
    }
}