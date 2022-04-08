package com.smackmap.smackmapbackend.smacker

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackerService(
    private val passwordEncoder: PasswordEncoder,
    private val smackerRepository: SmackerRepository,
    private val passwordRepository: PasswordRepository,
    private val authenticationManager: AuthenticationManager,
) {

    fun createSmacker(request: CreateSmackerRequest): Pair<Smacker, Password> {
        var smacker = Smacker(
            userName = request.userName,
            email = request.email,
            partner = null
        )
        smacker = smackerRepository.save(smacker)
        val password = Password(
            passwordHash = passwordEncoder.encode(request.password),
            smacker = smacker
        )
        passwordRepository.save(password)
        return Pair(smacker, password)
    }

    fun loginSmacker(request: LoginSmackerRequest): Pair<User, Smacker> {
        val smacker = if (request.email != null) {
            getSmackerByEmail(request.email)
        } else {
            getSmackerByUserName(request.userName!!)
        }
        val user: User = authenticateAndGetUser(smacker.userName, request.password)
        return Pair(user, smacker)
    }

    private fun authenticateAndGetUser(userName: String, password: String): User {
        val authentication: Authentication = authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(userName, password))
        return authentication.principal as User
    }

    fun getSmackerByUserName(userName: String): Smacker {
        return smackerRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by userName: $userName")
    }

    fun getSmackerByEmail(email: String): Smacker {
        return smackerRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by email: $email")
    }

    fun getPasswordOfSmacker(smacker: Smacker): Password {
        return passwordRepository.findBySmacker(smacker)
    }
}