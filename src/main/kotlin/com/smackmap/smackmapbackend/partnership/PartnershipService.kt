package com.smackmap.smackmapbackend.partnership

import com.smackmap.smackmapbackend.relation.Relation
import com.smackmap.smackmapbackend.relation.RelationService
import com.smackmap.smackmapbackend.relation.RelationStatus
import com.smackmap.smackmapbackend.relation.SmackerRelations
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
    private val partnershipStatuses = setOf(Partnership.Status.PARTNERSHIP_REQUESTED, Partnership.Status.PARTNER)

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

    suspend fun getPartnerships(smackerId: Long): List<Partnership> {
        val partnership = ArrayList<Partnership>(INITIAL_RELATION_LIST_SIZE)
        initiatorPartnershipsOfSmacker(smackerId).toList(partnership)
        respondentPartnershipsOfSmacker(smackerId).toList(partnership)
        return partnership
    }

    suspend fun requestPartnership(request: RequestPartnershipRequest): Partnership {
        relationService.checkBlockingBetweenSmackers(request.initiatorId, request.respondentId)
        relationService.checkPartnershipBetweenSmackers(request.initiatorId, request.respondentId)
        val initiatorPartnership: Partnership? = partnershipRepository.findByInitiatorIdAndRespondentId(
            request.initiatorId,
            request.respondentId,
        )
        if (initiatorPartnership != null) {
            // user already requested
            when (initiatorPartnership.status) {
                Partnership.Status.PARTNERSHIP_REQUESTED -> {
                    initiatorPartnership.initiateDate?.let {
                        if (hoursPassedSince(HOURS_TO_REREQUEST_PARTNERSHIP, it)) {
                            // TODO: send push notification
                            return initiatorPartnership
                        } else {
                            throw ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Already requested in the last '$HOURS_TO_REREQUEST_PARTNERSHIP' hours."
                            )
                        }
                    }
                }
                Partnership.Status.PARTNER -> {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "There is already an alive partnership between " +
                                "initiator '${request.initiatorId}' and respondent '${request.respondentId}'."
                    )
                }
                Relation.Status.BLOCKED -> {
                    throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User '${request.initiatorId}' blocked '${request.respondentId}', first need to unblock."
                    )
                }
            }
        } else {
            val respondentPartnership: Partnership? = partnershipRepository.findByInitiatorIdAndRespondentId(
                request.respondentId,
                request.initiatorId,
            )
            if (respondentPartnership != null) {
                when (respondentPartnership.status) {
                    Partnership.Status.PARTNERSHIP_REQUESTED -> {
                        throw ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "The other user '${request.respondentId}' already " +
                                    "requested partnership from you '${request.initiatorId}'."
                        )
                    }
                    Partnership.Status.PARTNER -> {
                        throw ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "There is already an alive partnership between " +
                                    "initiator '${request.initiatorId}' and respondent '${request.respondentId}'."
                        )
                    }
                    RelationStatus.BLOCKED -> {
                        throw ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User '${request.initiatorId}' is blocked by '${request.respondentId}'."
                        )
                    }
                }
            } else {
                val partnership = Partnership(
                    initiatorId = request.initiatorId,
                    respondentId = request.respondentId,
                    status = Partnership.Status.PARTNERSHIP_REQUESTED,
                    initiateDate = Timestamp.from(Instant.now())
                )
                // TODO: send push notification
                return partnershipRepository.save(partnership)
            }
        }
    }

    private fun hoursPassedSince(hours: Long, it: Timestamp) = Instant.now().minus(hours, HOURS).isAfter(it.toInstant())

    suspend fun respondPartnership(request: RespondPartnershipRequest): Relation {
        TODO("Not yet implemented")
    }

    suspend fun initiatorPartnershipsOfSmacker(smackerId: Long): Flow<Partnership> {
        return partnershipRepository.findByInitiatorId(smackerId)
    }

    suspend fun respondentPartnershipsOfSmacker(smackerId: Long): Flow<Partnership> {
        return partnershipRepository.findByRespondentId(smackerId)
    }
}