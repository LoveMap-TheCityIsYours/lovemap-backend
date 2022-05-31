package com.lovemap.lovemapbackend.security

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverRepository
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthorizationService(
    private val loverRepository: LoverRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun checkAccessFor(loverId: Long) {
        if (isAdmin()) {
            return
        }
        val caller = getCaller()
        if (loverId != caller.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun checkAccessFor(lover: Lover) {
        if (isAdmin()) {
            return
        }
        val caller = getCaller()
        if (lover.id != caller.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun checkAccessFor(loveSpot: LoveSpot) {
        if (isAdmin()) {
            return
        }
        val caller = getCaller()
        if (loveSpot.addedBy != caller.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun checkAccessFor(love: Love) {
        if (isAdmin()) {
            return
        }
        val caller = getCaller()
        if (love.loverId != caller.id && love.loverPartnerId != caller.id) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    suspend fun getCaller(): Lover {
        val securityContext = getSecurityContext()
        val userName = (securityContext.authentication.principal as User).username
        return loverRepository.findByUserName(userName)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    }

    suspend fun isAdmin(): Boolean {
        val securityContext = getSecurityContext()
        return securityContext.authentication.authorities
            .any { authority -> AUTHORITY_ADMIN == authority.authority }
    }

    private suspend fun getSecurityContext(): SecurityContext {
        val securityContext = ReactiveSecurityContextHolder.getContext().awaitSingleOrNull()
        if (securityContext == null) {
            logger.warn { "SecurityContext was null!" }
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
        return securityContext
    }
}