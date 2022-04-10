package com.smackmap.smackmapbackend.smacker

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

private const val LINK_PREFIX = "smacker://"

@Service
@Transactional
class SmackerService(
    private val smackerRepository: SmackerRepository,
) {
    suspend fun exists(id: Long): Boolean {
        return smackerRepository.existsById(id)
    }

    suspend fun getById(id: Long): Smacker {
        return smackerRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by id: $id")
    }

    suspend fun getByUserName(userName: String): Smacker {
        return smackerRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by userName: $userName")
    }

    suspend fun getByEmail(email: String): Smacker {
        return smackerRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by email: $email")
    }

    suspend fun save(smacker: Smacker): Smacker {
        return smackerRepository.save(smacker)
    }

    suspend fun generateSmackerLink(smackerId: Long): String {
        val smacker = getById(smackerId)
        if (smacker.link == null) {
            smacker.link = UUID.randomUUID().toString()
            save(smacker)
        }
        return "$LINK_PREFIX${smacker.link}"
    }

    suspend fun getByLink(link: String): Smacker {
        val uuidLink = link.substringAfter(LINK_PREFIX)
        return smackerRepository.findByLink(uuidLink)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by link: $link")
    }
}