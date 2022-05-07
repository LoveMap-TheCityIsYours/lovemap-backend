package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.security.AuthorizationService
import com.lovemap.lovemapbackend.utils.ErrorCode.*
import com.lovemap.lovemapbackend.utils.ErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
@Transactional
class LoverService(
    private val authorizationService: AuthorizationService,
    private val loverRepository: LoverRepository,
) {
    private val linkPrefix = "lovemap://lover/"

    suspend fun unAuthorizedExists(id: Long): Boolean {
        authorizationService.checkAccessFor(id)
        return loverRepository.existsById(id)
    }

    suspend fun getById(id: Long): Lover {
        return loverRepository.findById(id)?.also {
            authorizationService.checkAccessFor(it)
        } ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            ErrorMessage(
                NotFoundById,
                id.toString(),
                "Lover not found by id: '$id'."
            ).toJson()
        )
    }

    suspend fun unAuthorizedGetById(id: Long): Lover {
        return loverRepository.findById(id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundById,
                    id.toString(),
                    "Lover not found by id: '$id'."
                ).toJson()
            )
    }

    suspend fun unAuthorizedGetByUserName(userName: String): Lover {
        return loverRepository.findByUserName(userName)
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    InvalidCredentialsUser,
                    userName,
                    "Invalid credentials for userName: '$userName'."
                ).toJson()
            )
    }

    suspend fun unAuthorizedGetByEmail(email: String): Lover {
        return loverRepository.findByEmail(email)
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    InvalidCredentialsEmail,
                    email,
                    "Invalid credentials for email: '$email'."
                ).toJson()
            )
    }

    suspend fun save(lover: Lover): Lover {
        return loverRepository.save(lover)
    }

    suspend fun generateLoverLink(loverId: Long): Lover {
        var lover = authorizationService.checkAccessFor(loverId)
        if (lover.link == null) {
            lover.link = linkPrefix + UUID.randomUUID().toString()
            lover = save(lover)
        }
        return lover
    }

    suspend fun getByLink(link: String, caller: Lover): Lover {
        return loverRepository.findByLink(link)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundByLink,
                    link,
                    "Lover not found by link: '$link'."
                ).toJson()
            )
    }

    suspend fun checkUserNameAndEmail(userName: String, email: String) {
        if (loverRepository.findByUserName(userName) != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    UserOccupied,
                    userName,
                    "There is already a user with username '$userName'."
                ).toJson()
            )
        }
        if (loverRepository.findByEmail(email) != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    EmailOccupied,
                    email,
                    "There is already a user with email '$email'."
                ).toJson()
            )
        }
    }

    suspend fun deleteLoverLink(loverId: Long): Lover {
        var lover = authorizationService.checkAccessFor(loverId)
        lover.link = null
        return save(lover)
    }
}