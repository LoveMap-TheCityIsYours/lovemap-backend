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
    suspend fun checkAccessFor(smackerId: Long) {
        val securityContext = ReactiveSecurityContextHolder.getContext().awaitSingle()
        val userName = (securityContext.authentication.principal as User).username
        val smackerByUserName = smackerRepository.findByUserName(userName)
        if (smackerByUserName == null || smackerId != smackerByUserName.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun checkAccessFor(smacker: Smacker) {
        val securityContext = ReactiveSecurityContextHolder.getContext().awaitSingle()
        val userName = (securityContext.authentication.principal as User).username
        val smackerByUserName = smackerRepository.findByUserName(userName)
        if (smackerByUserName == null || smacker.id != smackerByUserName.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }
}