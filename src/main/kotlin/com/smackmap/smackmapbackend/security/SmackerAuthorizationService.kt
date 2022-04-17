package com.smackmap.smackmapbackend.security

import com.smackmap.smackmapbackend.smacker.Smacker
import com.smackmap.smackmapbackend.smacker.SmackerRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class SmackerAuthorizationService(
    private val smackerRepository: SmackerRepository
) {
    suspend fun checkAccessFor(smackerId: Long): Smacker {
        val caller = getCaller()
        if (smackerId != caller.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        return caller
    }

    suspend fun checkAccessFor(smacker: Smacker) {
        val smackerByUserName = getCaller()
        if (smacker.id != smackerByUserName.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun getCaller(): Smacker {
        val securityContext = ReactiveSecurityContextHolder.getContext().awaitSingle()
        val userName = (securityContext.authentication.principal as User).username
        return smackerRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    }
}