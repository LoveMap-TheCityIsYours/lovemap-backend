package com.lovemap.lovemapbackend.security

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverRepository
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val loverRepository: LoverRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun checkAccessFor(loverId: Long): Lover {
        val caller = getCaller()
        if (isAdmin()) {
            return caller
        }
        if (loverId != caller.id) {
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorMessage(ErrorCode.Forbidden))
        }
        return caller
    }

    suspend fun checkAccessFor(lover: Lover): Lover {
        val caller = getCaller()
        if (isAdmin()) {
            return caller
        }
        if (lover.id != caller.id) {
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorMessage(ErrorCode.Forbidden))
        }
        return caller
    }

    suspend fun checkAccessFor(loveSpot: LoveSpot): Lover {
        val caller = getCaller()
        if (isAdmin()) {
            return caller
        }
        if (loveSpot.addedBy != caller.id) {
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorMessage(ErrorCode.Forbidden))
        }
        return caller
    }

    suspend fun checkAccessFor(love: Love): Lover {
        val caller = getCaller()
        if (isAdmin()) {
            return caller
        }
        if (love.loverId != caller.id && love.loverPartnerId != caller.id) {
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorMessage(ErrorCode.Forbidden))
        }
        return caller
    }

    suspend fun getCaller(): Lover {
        val securityContext = getSecurityContext()
        val userName = (securityContext.authentication.principal as User).username
        return loverRepository.findByUserName(userName)
            ?: throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorMessage(ErrorCode.Forbidden))
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
            throw LoveMapException(HttpStatus.UNAUTHORIZED, ErrorMessage(ErrorCode.Forbidden))
        }
        return securityContext
    }
}