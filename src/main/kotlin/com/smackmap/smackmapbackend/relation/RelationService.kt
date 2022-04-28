package com.smackmap.smackmapbackend.relation

import com.smackmap.smackmapbackend.relation.Relation.Status.FOLLOWING
import com.smackmap.smackmapbackend.relation.Relation.Status.PARTNER
import com.smackmap.smackmapbackend.smacker.SmackerService
import com.smackmap.smackmapbackend.utils.ErrorCode
import com.smackmap.smackmapbackend.utils.ErrorMessage
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class RelationService(
    private val smackerService: SmackerService,
    private val relationRepository: RelationRepository,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun checkBlockingBetweenSmackers(initiatorId: Long, respondentId: Long) {
        if (relationRepository.existsBySourceIdAndTargetIdAndStatus(
                initiatorId,
                respondentId,
                Relation.Status.BLOCKED
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.YouBlockedHimUnblockFirst,
                    respondentId.toString(),
                    "User '$initiatorId' blocked '$respondentId', first need to unblock."
                ).toJson()
            )
        }
        if (relationRepository.existsBySourceIdAndTargetIdAndStatus(
                respondentId,
                initiatorId,
                Relation.Status.BLOCKED
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    ErrorCode.BlockedByUser,
                    respondentId.toString(),
                    "User '$initiatorId' is blocked by '$respondentId'."
                ).toJson()
            )
        }
    }

    suspend fun setPartnershipBetween(user1: Long, user2: Long) {
        if (user1 == user2) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    user1.toString(),
                    "Source and Target in a relation cannot be the same. '$user1'"
                ).toJson()
            )
        }
        val relation12: Relation = relationRepository.findBySourceIdAndTargetId(user1, user2)
            ?: Relation(status = PARTNER, sourceId = user1, targetId = user2)
        relation12.status = PARTNER
        relationRepository.save(relation12)

        val relation21: Relation = relationRepository.findBySourceIdAndTargetId(user2, user1)
            ?: Relation(status = PARTNER, sourceId = user2, targetId = user1)
        relation21.status = PARTNER
        relationRepository.save(relation21)
    }

    suspend fun getRelation(fromId: Long, toId: Long): Relation {
        return relationRepository.findBySourceIdAndTargetId(fromId, toId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.RelationNotFound,
                    toId.toString(),
                    "Relation not found from '$fromId' to '$toId'."
                ).toJson()
            )
    }

    suspend fun getRelationStatusDto(fromId: Long, toId: Long): RelationStatusDto {
        return RelationStatusDto.of(relationRepository.findBySourceIdAndTargetId(fromId, toId)?.status)
    }

    suspend fun getRelationsFrom(fromId: Long): SmackerRelations {
        val relationFlow = relationRepository.findBySourceIdAndStatusIn(fromId, setOf(FOLLOWING, PARTNER))
        val smackerRelationFlow = relationFlow.map { value: Relation ->
            val inRelationWith = smackerService.unAuthorizedGetById(value.targetId)
            SmackerRelation(
                inRelationWith.toView(),
                inRelationWith.rank,
                value.status
            )
        }
        return SmackerRelations(fromId, smackerRelationFlow)
    }

    suspend fun checkPartnership(smackerId: Long, partnerId: Long) {
        checkBlockingBetweenSmackers(smackerId, partnerId)
        if (!relationRepository.existsBySourceIdAndTargetIdAndStatus(smackerId, partnerId, PARTNER)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.PartnershipNotFound,
                    partnerId.toString(),
                    "No partnership between '$smackerId' and '$partnerId'"
                ).toJson()
            )
        }
    }
}