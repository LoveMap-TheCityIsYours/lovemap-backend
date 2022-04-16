package com.smackmap.smackmapbackend.partnership

import com.smackmap.smackmapbackend.partnership.Partnership.Status.PARTNER
import com.smackmap.smackmapbackend.partnership.Partnership.Status.PARTNERSHIP_REQUESTED
import com.smackmap.smackmapbackend.partnership.PartnershipReaction.ACCEPT
import com.smackmap.smackmapbackend.partnership.PartnershipReaction.DENY
import com.smackmap.smackmapbackend.relation.Relation
import com.smackmap.smackmapbackend.relation.RelationService
import com.smackmap.smackmapbackend.security.SmackerAuthorizationService
import com.smackmap.smackmapbackend.smacker.SmackerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

private const val INITIAL_RELATION_LIST_SIZE = 30
private const val HOURS_TO_REREQUEST_PARTNERSHIP: Long = 12

@Service
@Transactional
class PartnershipService(
    private val authorizationService: SmackerAuthorizationService,
    private val partnershipRepository: PartnershipRepository,
    private val relationService: RelationService,
    private val smackerService: SmackerService
) {
    private val logger = KotlinLogging.logger {}

    suspend fun getSmackerPartnerships(smackerId: Long): SmackerPartnerships {
        authorizationService.checkAccessFor(smackerId)
        if (smackerService.exists(smackerId)) {
            return SmackerPartnerships(
                smackerId = smackerId,
                relations = getPartnerships(smackerId)
            )
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Smacker not found by ID '$smackerId'")
        }
    }

    suspend fun requestPartnership(request: RequestPartnershipRequest): Partnership {
        val initiatorId = request.initiatorId
        val respondentId = request.respondentId
        authorizationService.checkAccessFor(initiatorId)
        validateInitiatorRespondentIds(initiatorId, respondentId)
        relationService.checkBlockingBetweenSmackers(initiatorId, respondentId)
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

    suspend fun respondToPartnershipRequest(request: RespondPartnershipRequest): SmackerPartnerships {
        val initiatorId = request.initiatorId
        val respondentId = request.respondentId
        authorizationService.checkAccessFor(respondentId)
        val partnership = validatePartnershipResponse(initiatorId, respondentId)
        return when (partnership.status) {
            PARTNER -> throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "User '$initiatorId' and user '$respondentId' are already partners."
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
        validateInitiatorRespondentIds(initiatorId, respondentId)
        try {
            relationService.checkBlockingBetweenSmackers(initiatorId, respondentId)
        } catch (e: ResponseStatusException) {
            denyPartnershipRequest(initiatorId, respondentId)
            throw e
        }
        return partnershipRepository.findByInitiatorIdAndRespondentId(initiatorId, respondentId)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Partnership was never requested from user '$initiatorId' to user '$respondentId'."
            )
    }

    private suspend fun handlePartnershipResponse(
        request: RespondPartnershipRequest,
        partnership: Partnership,
        respondentId: Long
    ): SmackerPartnerships {
        return when (request.response) {
            ACCEPT -> {
                partnership.status = PARTNER
                partnership.respondDate = Timestamp.from(Instant.now())
                partnershipRepository.save(partnership)
                relationService.updateRelations(
                    partnership.initiatorId,
                    partnership.respondentId,
                    Relation.Status.PARTNER
                )
                sendPushNotification(partnership)
                getSmackerPartnerships(respondentId)
            }
            DENY -> {
                partnershipRepository.delete(partnership)
                getSmackerPartnerships(respondentId)
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
                    if (hoursPassedSince(HOURS_TO_REREQUEST_PARTNERSHIP, it)) {
                        sendPushNotification(initiatorPartnership)
                        return initiatorPartnership
                    } else {
                        throw ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Already requested in the last '$HOURS_TO_REREQUEST_PARTNERSHIP' hours."
                        )
                    }
                }
            }
            PARTNER -> {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "There is already an alive partnership between " +
                            "initiator '${request.initiatorId}' and respondent '${request.respondentId}'."
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
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The other user '${request.respondentId}' already " +
                            "requested partnership from you '${request.initiatorId}'."
                )
            }
            PARTNER -> {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "There is already an alive partnership between " +
                            "initiator '${request.initiatorId}' and respondent '${request.respondentId}'."
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

    private suspend fun getPartnerships(smackerId: Long): List<Partnership> {
        val partnership = ArrayList<Partnership>(INITIAL_RELATION_LIST_SIZE)
        initiatorPartnershipsOfSmacker(smackerId).toList(partnership)
        respondentPartnershipsOfSmacker(smackerId).toList(partnership)
        return partnership
    }

    private suspend fun initiatorPartnershipsOfSmacker(smackerId: Long): Flow<Partnership> {
        return partnershipRepository.findByInitiatorId(smackerId)
    }

    private suspend fun respondentPartnershipsOfSmacker(smackerId: Long): Flow<Partnership> {
        return partnershipRepository.findByRespondentId(smackerId)
    }

    private fun validateInitiatorRespondentIds(initiatorId: Long, respondentId: Long) {
        if (initiatorId == respondentId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "InitiatorId and respondentId cannot be the same! '${initiatorId}'"
            )
        }
    }

    private fun hoursPassedSince(hours: Long, it: Timestamp) = Instant.now().minus(hours, HOURS).isAfter(it.toInstant())
}