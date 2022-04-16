package com.smackmap.smackmapbackend.partnership

import com.smackmap.smackmapbackend.partnership.Partnership.Status.PARTNER
import com.smackmap.smackmapbackend.partnership.Partnership.Status.PARTNERSHIP_REQUESTED
import com.smackmap.smackmapbackend.relation.RelationService
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
    private val partnershipRepository: PartnershipRepository,
    private val relationService: RelationService,
    private val smackerService: SmackerService
) {
    private val logger = KotlinLogging.logger {}

    suspend fun getSmackerPartnerships(smackerId: Long): SmackerPartnerships {
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
        relationService.checkBlockingBetweenSmackers(request.initiatorId, request.respondentId)
        val initiatorPartnership: Partnership? = getInitiatorPartnership(request.initiatorId, request.respondentId)
        return if (initiatorPartnership != null) {
            handleInitiatorPartnership(initiatorPartnership, request)
        } else {
            val respondentPartnership: Partnership? =
                getRespondentPartnership(request.initiatorId, request.respondentId)
            if (respondentPartnership != null) {
                handleRespondentPartnership(respondentPartnership, request)
            } else {
                createPartnershipRequest(request)
            }
        }
    }

    suspend fun respondToPartnershipRequest(request: RespondPartnershipRequest): Partnership {
        val initiatorId = request.initiatorId
        val respondentId = request.respondentId
        try {
            relationService.checkBlockingBetweenSmackers(initiatorId, respondentId)
        } catch (e: ResponseStatusException) {
            denyPartnershipRequest(initiatorId, respondentId)
            throw e
        }
        val partnership = partnershipRepository.findByInitiatorIdAndRespondentId(initiatorId, respondentId)
        return if (partnership == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Partnership was never requested from user '$initiatorId' to user '$respondentId'."
            )
        } else {
            when (partnership.status) {
                PARTNER -> throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User '$initiatorId' and user '$respondentId' are already partners."
                )
                PARTNERSHIP_REQUESTED -> {
                    partnership.status = PARTNER
                    partnershipRepository.save(partnership)
                    sendPushNotification(partnership)
                    partnership
                }
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

    private fun hoursPassedSince(hours: Long, it: Timestamp) = Instant.now().minus(hours, HOURS).isAfter(it.toInstant())
}