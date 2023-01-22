package com.lovemap.lovemapbackend.lover.partnership

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverService
import com.lovemap.lovemapbackend.lover.partnership.Partnership.Status.PARTNER
import com.lovemap.lovemapbackend.lover.partnership.Partnership.Status.PARTNERSHIP_REQUESTED
import com.lovemap.lovemapbackend.lover.partnership.PartnershipReaction.ACCEPT
import com.lovemap.lovemapbackend.lover.partnership.PartnershipReaction.DENY
import com.lovemap.lovemapbackend.lover.relation.RelationService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

@Service
@Transactional
class PartnershipService(
    private val authorizationService: AuthorizationService,
    private val relationService: RelationService,
    private val loverService: LoverService,
    private val partnershipRepository: PartnershipRepository,
) {
    private val hoursToRerequestPartnership: Long = 12
    private val logger = KotlinLogging.logger {}

    suspend fun getPartnerOf(loverId: Long): Lover? {
        val partnership: Partnership? = getLoverPartnership(loverId).partnership?.takeIf {
            it.status == PARTNER
        }
        return partnership?.partnerOf(loverId)
            ?.let { partnerId -> loverService.unAuthorizedGetById(partnerId) }
    }

    suspend fun getLoverPartnership(loverId: Long): LoverPartnership {
        if (loverService.unAuthorizedExists(loverId)) {
            return LoverPartnership(
                loverId = loverId,
                partnership = getPartnership(loverId)
            )
        } else {
            throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.PartnershipNotFound,
                    loverId.toString(),
                    "Lover not found by ID '$loverId'"
                )
            )
        }
    }

    suspend fun requestPartnership(request: RequestPartnershipRequest): Partnership {
        val initiatorId = request.initiatorId
        val respondentId = request.respondentId
        authorizationService.checkAccessFor(initiatorId)
        if (getLoverPartnership(request.initiatorId).partnership != null) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.AlreadyHasPartner)
        }
        if (getLoverPartnership(request.respondentId).partnership != null) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.RespondentAlreadyHasPartner)
        }
        validateInitiatorRespondentNotTheSame(initiatorId, respondentId)
        relationService.checkBlockingBetweenLovers(initiatorId, respondentId)
        val initiatorPartnership: Partnership? = getInitiatorPartnership(initiatorId, respondentId)
        return if (initiatorPartnership != null) {
            handleInitiatorPartnership(initiatorPartnership, request)
        } else {
            val respondentPartnership: Partnership? =
                getRespondentPartnership(initiatorId, respondentId)
            if (respondentPartnership != null) {
                handleRespondentPartnership(respondentPartnership, request)
            } else {
                createPartnershipRequest(request)
            }
        }
    }

    suspend fun cancelPartnershipRequest(loverId: Long, request: CancelPartnershipRequest): LoverPartnership {
        val initiatorId = request.initiatorId
        authorizationService.checkAccessFor(initiatorId)
        val respondentId = request.respondentId
        getLoverPartnership(loverId).partnership
            ?.takeIf { it.initiatorId == initiatorId && it.respondentId == respondentId }
            ?.let { partnership ->
                partnershipRepository.delete(partnership)
            } ?: throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.PartnershipNotFound)
        return getLoverPartnership(loverId)
    }

    suspend fun endPartnership(loverId: Long, partnerLoverId: Long): LoverPartnership {
        authorizationService.checkAccessFor(loverId)
        getLoverPartnership(loverId).partnership
            ?.takeIf { it.partnerOf(loverId) == partnerLoverId }
            ?.let { partnership ->
                partnershipRepository.delete(partnership)
                loverService.removePartnershipBetween(loverId, partnerLoverId)
                relationService.removePartnershipBetween(loverId, partnerLoverId)
            } ?: throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.PartnershipNotFound)
        return getLoverPartnership(loverId)
    }

    suspend fun respondToPartnershipRequest(request: RespondPartnershipRequest): LoverPartnership {
        val initiatorId = request.initiatorId
        val respondentId = request.respondentId
        authorizationService.checkAccessFor(respondentId)
        val partnership = validatePartnershipResponse(initiatorId, respondentId)
        return when (partnership.status) {
            PARTNER -> throw LoveMapException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    ErrorCode.AlreadyPartners,
                    initiatorId.toString(),
                    "User '$initiatorId' and user '$respondentId' are already partners."
                )
            )
            PARTNERSHIP_REQUESTED -> {
                handlePartnershipResponse(request, partnership, respondentId)
            }
        }
    }

    private suspend fun validatePartnershipResponse(
        initiatorId: Long,
        respondentId: Long
    ): Partnership {
        validateInitiatorRespondentNotTheSame(initiatorId, respondentId)
        try {
            relationService.checkBlockingBetweenLovers(initiatorId, respondentId)
        } catch (e: ResponseStatusException) {
            denyPartnershipRequest(initiatorId, respondentId)
            throw e
        }
        return partnershipRepository.findByInitiatorIdAndRespondentId(initiatorId, respondentId)
            ?: throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    initiatorId.toString(),
                    "Partnership was never requested from user '$initiatorId' to user '$respondentId'."
                )
            )
    }

    @Transactional
    private suspend fun handlePartnershipResponse(
        request: RespondPartnershipRequest,
        partnership: Partnership,
        respondentId: Long
    ): LoverPartnership {
        return when (request.response) {
            ACCEPT -> {
                partnership.status = PARTNER
                partnership.respondDate = Timestamp.from(Instant.now())
                partnershipRepository.save(partnership)
                relationService.setPartnershipBetween(
                    partnership.initiatorId,
                    partnership.respondentId
                )
                loverService.setPartnershipBetween(
                    partnership.initiatorId,
                    partnership.respondentId
                )
                sendPushNotification(partnership)
                getLoverPartnership(respondentId)
            }
            DENY -> {
                partnershipRepository.delete(partnership)
                getLoverPartnership(respondentId)
            }
        }
    }

    private suspend fun denyPartnershipRequest(initiatorId: Long, respondentId: Long) {
        val initiatorPartnership = getInitiatorPartnership(initiatorId, respondentId)
        if (initiatorPartnership != null) {
            partnershipRepository.delete(initiatorPartnership)
        } else {
            val respondentPartnership = getRespondentPartnership(initiatorId, respondentId)
            respondentPartnership?.let {
                partnershipRepository.delete(it)
            }
        }
    }

    private suspend fun getRespondentPartnership(
        initiatorId: Long,
        respondentId: Long
    ): Partnership? = partnershipRepository.findByInitiatorIdAndRespondentId(respondentId, initiatorId)


    private suspend fun getInitiatorPartnership(
        initiatorId: Long,
        respondentId: Long
    ): Partnership? = partnershipRepository.findByInitiatorIdAndRespondentId(initiatorId, respondentId)

    private fun handleInitiatorPartnership(
        initiatorPartnership: Partnership,
        request: RequestPartnershipRequest
    ): Partnership {
        when (initiatorPartnership.status) {
            PARTNERSHIP_REQUESTED -> {
                initiatorPartnership.initiateDate!!.let {
                    if (hoursPassedSince(hoursToRerequestPartnership, it)) {
                        sendPushNotification(initiatorPartnership)
                        return initiatorPartnership
                    } else {
                        throw LoveMapException(
                            HttpStatus.CONFLICT,
                            ErrorMessage(
                                ErrorCode.PartnershipRerequestTimeNotPassed,
                                hoursToRerequestPartnership.toString(),
                                "Already requested in the last '$hoursToRerequestPartnership' hours."
                            )
                        )
                    }
                }
            }
            PARTNER -> {
                throw LoveMapException(
                    HttpStatus.CONFLICT,
                    ErrorMessage(
                        ErrorCode.AlreadyPartners,
                        request.respondentId.toString(),
                        "There is already an alive partnership between " +
                                "initiator '${request.initiatorId}' and respondent '${request.respondentId}'."
                    )
                )
            }
        }
    }

    private fun handleRespondentPartnership(
        respondentPartnership: Partnership,
        request: RequestPartnershipRequest
    ): Partnership {
        when (respondentPartnership.status) {
            PARTNERSHIP_REQUESTED -> {
                throw LoveMapException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessage(
                        ErrorCode.PartnershipAlreadyRequested,
                        request.respondentId.toString(),
                        "The other user '${request.respondentId}' already " +
                                "requested partnership from you '${request.initiatorId}'."
                    )
                )
            }
            PARTNER -> {
                throw LoveMapException(
                    HttpStatus.CONFLICT,
                    ErrorMessage(
                        ErrorCode.AlreadyPartners,
                        request.respondentId.toString(),
                        "There is already an alive partnership between " +
                                "initiator '${request.initiatorId}' and respondent '${request.respondentId}'."
                    )
                )
            }
        }
    }

    private suspend fun createPartnershipRequest(request: RequestPartnershipRequest): Partnership {
        val partnership = Partnership(
            initiatorId = request.initiatorId,
            respondentId = request.respondentId,
            status = PARTNERSHIP_REQUESTED,
            initiateDate = Timestamp.from(Instant.now())
        )
        sendPushNotification(partnership)
        return partnershipRepository.save(partnership)
    }

    private fun sendPushNotification(partnership: Partnership) {
        // TODO: implement
    }

    private suspend fun getPartnership(loverId: Long): Partnership? {
        return (initiatorPartnershipsOfLover(loverId).toSet() +
                respondentPartnershipsOfLover(loverId).toSet()).firstOrNull()

    }

    private suspend fun initiatorPartnershipsOfLover(loverId: Long): Flow<Partnership> {
        return partnershipRepository.findByInitiatorId(loverId)
    }

    private suspend fun respondentPartnershipsOfLover(loverId: Long): Flow<Partnership> {
        return partnershipRepository.findByRespondentId(loverId)
    }

    private fun validateInitiatorRespondentNotTheSame(initiatorId: Long, respondentId: Long) {
        if (initiatorId == respondentId) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.InvalidOperationOnYourself,
                    initiatorId.toString(),
                    "InitiatorId and respondentId cannot be the same! '${initiatorId}'"
                )
            )
        }
    }

    private fun hoursPassedSince(hours: Long, it: Timestamp) = Instant.now().minus(hours, HOURS).isAfter(it.toInstant())

}
