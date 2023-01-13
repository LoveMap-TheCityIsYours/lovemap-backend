package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.newfeed.LoverNewsFeedUpdater
import com.lovemap.lovemapbackend.utils.ErrorCode.*
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

@Service
@Transactional
class LoverService(
    private val authorizationService: AuthorizationService,
    private val converter: LoverConverter,
    private val loverNewsFeedUpdater: LoverNewsFeedUpdater,
    private val loverRepository: LoverRepository,
) {
    companion object {
        const val linkPrefixApiCall = "https://api.lovemap.app/lover?uuid="
        const val linkPrefixVisible = "https://api.lovemap.app/join-us/lover?uuid="
    }

    suspend fun unAuthorizedExists(id: Long): Boolean {
        return loverRepository.existsById(id)
    }

    suspend fun getById(id: Long): Lover {
        return loverRepository.findById(id)?.also {
            authorizationService.checkAccessFor(it)
        } ?: throw LoveMapException(
            HttpStatus.NOT_FOUND,
            ErrorMessage(
                NotFoundById,
                id.toString(),
                "Lover not found by id: '$id'."
            )
        )
    }

    suspend fun unAuthorizedGetById(id: Long): Lover {
        return loverRepository.findById(id)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundById,
                    id.toString(),
                    "Lover not found by id: '$id'."
                )
            )
    }

    suspend fun unAuthorizedGetByUserName(userName: String): Lover {
        return loverRepository.findByUserName(userName)
            ?: throw LoveMapException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    InvalidCredentialsUser,
                    userName,
                    "Invalid credentials for userName: '$userName'."
                )
            )
    }

    suspend fun unAuthorizedGetByEmail(email: String): Lover {
        return loverRepository.findByEmail(email)
            ?: throw LoveMapException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    InvalidCredentialsEmail,
                    email,
                    "Invalid credentials for email: '$email'."
                )
            )
    }

    suspend fun save(lover: Lover): Lover {
        return loverRepository.save(lover)
    }

    suspend fun generateLoverUuid(loverId: Long): Lover {
        var lover = authorizationService.checkAccessFor(loverId)
        if (lover.uuid == null) {
            lover.uuid = UUID.randomUUID().toString()
            lover = save(lover)
        }
        return lover
    }

    suspend fun getByUuid(uuid: String, caller: Lover): Lover {
        return loverRepository.findByUuid(uuid)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundByLink,
                    uuid,
                    "Lover not found by uuid: '$uuid'."
                )
            )
    }

    suspend fun checkUserNameAndEmail(userName: String, email: String) {
        if (loverRepository.findByUserName(userName) != null) {
            throw LoveMapException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    UserOccupied,
                    userName,
                    "There is already a user with username '$userName'."
                )
            )
        }
        if (loverRepository.findByEmail(email) != null) {
            throw LoveMapException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    EmailOccupied,
                    email,
                    "There is already a user with email '$email'."
                )
            )
        }
    }

    suspend fun deleteLoverLink(loverId: Long): Lover {
        val lover = authorizationService.checkAccessFor(loverId)
        lover.uuid = null
        return save(lover)
    }

    suspend fun unAuthorizedExistsByEmail(email: String): Boolean {
        return loverRepository.existsByEmail(email)
    }

    fun getLoversFrom(generateFrom: Instant): Flow<Lover> {
        return loverRepository.findAllAfterCreatedAt(Timestamp.from(generateFrom))
    }

    suspend fun updateLover(loverId: Long, update: UpdateLoverRequest): LoverResponse {
        val lover = getById(loverId)
        update.email?.let {
            checkUserNameAndEmail(lover.userName, it)
            lover.email = it
        }
        update.displayName?.let { lover.displayName = it }
        val savedLover = save(lover)
        update.displayName?.let { loverNewsFeedUpdater.updateLoverNameChange(loverId, it) }
        return converter.toResponse(savedLover)
    }

    suspend fun getAllByIds(loverIds: Set<Long>): Map<Long, Lover> {
        return loverRepository.findAllById(loverIds).toSet().associateBy { it.id }
    }
}