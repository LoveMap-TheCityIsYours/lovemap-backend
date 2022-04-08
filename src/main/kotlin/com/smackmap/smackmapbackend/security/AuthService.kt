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
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val smackerService: SmackerService,
    private val passwordRepository: PasswordService,
    private val authenticationManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {

    fun createSmacker(request: CreateSmackerRequest): Pair<Smacker, Password> {
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
        return Pair(smacker, password)
    }

    fun loginSmacker(request: LoginSmackerRequest): Pair<User, Smacker> {
        val smacker = if (request.email != null) {
            smackerService.getSmackerByEmail(request.email)
        } else {
            smackerService.getSmackerByUserName(request.userName!!)
        }
        val user: User = authenticateAndGetUser(smacker.userName, request.password)
        return Pair(user, smacker)
    }

    private fun authenticateAndGetUser(userName: String, password: String): User {
        val authentication: Authentication = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(userName, password))
        return authentication.principal as User
    }
}