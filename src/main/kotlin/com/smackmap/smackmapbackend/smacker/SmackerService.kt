package com.smackmap.smackmapbackend.smacker

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackerService(
    private val passwordEncoder: PasswordEncoder,
    private val smackerRepository: SmackerRepository,
    private val passwordRepository: PasswordRepository
) {

    fun createSmacker(request: CreateSmackerRequest): SmackerResponse {
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
        return SmackerResponse.of(smacker)
    }

    fun getSmackerByUserName(userName: String): Smacker {
        return smackerRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by userName: $userName")
    }

    fun getPasswordBySmacker(smacker: Smacker): Password {
        return passwordRepository.findBySmacker(smacker)
    }
}