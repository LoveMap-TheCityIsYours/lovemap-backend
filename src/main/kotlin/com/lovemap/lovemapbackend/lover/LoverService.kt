package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.newsfeed.LoverNewsFeedUpdater
import com.lovemap.lovemapbackend.tracking.UserTrackingService
import com.lovemap.lovemapbackend.utils.ErrorCode.*
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.*

private const val HALL_OF_FAME_SIZE = 100

@Service
class LoverService(
    private val authorizationService: AuthorizationService,
    private val converter: LoverConverter,
    private val loverNewsFeedUpdater: LoverNewsFeedUpdater,
    private val cachedLoverService: CachedLoverService,
    private val userTrackingService: UserTrackingService,
    private val repository: LoverRepository,
) {
    companion object {
        const val linkPrefixVisible = "https://api.lovemap.app/join-us/lover?uuid="
    }

    private val logger = KotlinLogging.logger {}

    suspend fun unAuthorizedExists(id: Long): Boolean {
        return repository.existsById(id)
    }

    suspend fun getById(id: Long): Lover {
        return repository.findById(id)?.let { lover ->
            authorizationService.checkAccessFor(lover)
            cachedLoverService.put(lover)
            lover
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
        return repository.findById(id)?.let { lover ->
            cachedLoverService.put(lover)
            lover
        } ?: throw LoveMapException(
            HttpStatus.NOT_FOUND,
            ErrorMessage(
                NotFoundById,
                id.toString(),
                "Lover not found by id: '$id'."
            )
        )
    }

    suspend fun unAuthorizedGetByUserName(userName: String): Lover {
        return repository.findByUserName(userName)
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
        return repository.findByEmail(email)
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
        return repository.save(lover)
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
        return repository.findByUuid(uuid)
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
        if (repository.findByUserName(userName) != null) {
            throw LoveMapException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    UserOccupied,
                    userName,
                    "There is already a user with username '$userName'."
                )
            )
        }
        if (repository.findByEmail(email) != null) {
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
        return repository.existsByEmail(email)
    }

    fun getLoversFrom(generateFrom: Instant): Flow<Lover> {
        return repository.findAllAfterCreatedAt(Timestamp.from(generateFrom))
    }

    @Transactional
    suspend fun updateLover(loverId: Long, update: UpdateLoverRequest): LoverResponse {
        logger.info { "Received UpdateLoverRequest $update" }
        val lover: Lover = getById(loverId)
        update.email?.let {
            checkUserNameAndEmail(lover.userName, it)
            lover.email = it
        }
        update.displayName?.let { lover.displayName = it }
        update.publicProfile?.let { lover.publicProfile = it }
        val savedLover = save(lover)
        cachedLoverService.put(savedLover)
        logger.info { "Updated Lover $savedLover" }
        update.displayName?.let { loverNewsFeedUpdater.updateLoverNameChange(loverId, it) }
        return converter.toResponse(savedLover)
    }

    suspend fun getAllByIds(loverIds: Set<Long>): Map<Long, Lover> {
        return repository.findAllById(loverIds).toSet().associateBy { it.id }
    }

    suspend fun setPartnershipBetween(initiatorId: Long, respondentId: Long) {
        val initiator = unAuthorizedGetById(initiatorId)
        initiator.partnerId = respondentId
        save(initiator)

        val respondent = unAuthorizedGetById(respondentId)
        respondent.partnerId = initiatorId
        save(respondent)
    }

    suspend fun removePartnershipBetween(loverId: Long, partnerLoverId: Long) {
        val lover = unAuthorizedGetById(loverId)
        lover.partnerId = null
        save(lover)

        val partner = unAuthorizedGetById(partnerLoverId)
        partner.partnerId = null
        save(partner)
    }

    suspend fun getHallOfFame(): List<LoverViewWithoutRelationResponse> {
        return repository.findTopLimitOrderByPoints(HALL_OF_FAME_SIZE)
            .map {
                if (it.publicProfile) {
                    LoverViewWithoutRelationResponse.of(it)
                } else {
                    LoverViewWithoutRelationResponse.of(it).copy(displayName = "")
                }
            }
            .toList()
    }

    suspend fun registerFirebaseToken(loverId: Long, token: FirebaseTokenRegistration): LoverResponse {
        val caller = authorizationService.checkIdentity(loverId)
        caller.firebaseToken = token.token
        caller.hasFirebaseToken = true
        userTrackingService.saveFirebaseToken(loverId, token.token)
        return converter.toResponse(repository.save(caller))
    }
}
