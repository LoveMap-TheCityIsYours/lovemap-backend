package com.lovemap.lovemapbackend.security

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthorizationService(
    private val loverRepository: LoverRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun checkAccessFor(loverId: Long): Lover {
        val caller = getCaller()
        if (loverId != caller.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        return caller
    }

    suspend fun checkAccessFor(lover: Lover) {
        val loverByUserName = getCaller()
        if (lover.id != loverByUserName.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun getCaller(): Lover {
        val securityContext = ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()
        if (securityContext == null) {
            logger.warn { "SecurityContext was null!" }
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        val userName = (securityContext.authentication.principal as User).username
        return loverRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    }
}