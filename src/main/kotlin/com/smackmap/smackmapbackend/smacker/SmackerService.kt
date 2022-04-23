package com.smackmap.smackmapbackend.smacker

import com.smackmap.smackmapbackend.security.AuthorizationService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
@Transactional
class SmackerService(
    private val authorizationService: AuthorizationService,
    private val smackerRepository: SmackerRepository,
) {
    private val linkPrefix = "smacker://"

    suspend fun exists(id: Long): Boolean {
        authorizationService.checkAccessFor(id)
        return smackerRepository.existsById(id)
    }

    suspend fun getById(id: Long): Smacker {
        return smackerRepository.findById(id)?.also {
            authorizationService.checkAccessFor(it)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by id: $id")
    }

    suspend fun unAuthorizedGetByUserName(userName: String): Smacker {
        return smackerRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by userName: $userName")
    }

    suspend fun unAuthorizedGetByEmail(email: String): Smacker {
        return smackerRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by email: $email")
    }

    suspend fun save(smacker: Smacker): Smacker {
        return smackerRepository.save(smacker)
    }

    suspend fun generateSmackerLink(smackerId: Long): Smacker {
        var smacker = authorizationService.checkAccessFor(smackerId)
        if (smacker.link == null) {
            smacker.link = UUID.randomUUID().toString()
            smacker = save(smacker)
        }
        return smacker
    }

    suspend fun getByLink(link: String, caller: Smacker): Smacker {
        val uuidLink = link.substringAfter(linkPrefix)
        return smackerRepository.findByLink(uuidLink)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by link: $link")
    }

    suspend fun checkUserNameAndEmail(userName: String, email: String) {
        if (smackerRepository.findByUserName(userName) != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "There is already a user with username '$userName'."
            )
        }
        if (smackerRepository.findByEmail(email) != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "There is already a user with email '$email'."
            )
        }
    }

    suspend fun deleteSmackerLink(smackerId: Long): Smacker {
        var smacker = authorizationService.checkAccessFor(smackerId)
        smacker.link = null
        return save(smacker)
    }
}