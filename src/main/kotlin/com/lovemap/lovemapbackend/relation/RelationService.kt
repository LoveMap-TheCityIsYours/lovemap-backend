package com.lovemap.lovemapbackend.relation

import com.lovemap.lovemapbackend.lover.LoverService
import com.lovemap.lovemapbackend.relation.Relation.Status.FOLLOWING
import com.lovemap.lovemapbackend.relation.Relation.Status.PARTNER
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RelationService(
    private val loverService: LoverService,
    private val relationRepository: RelationRepository,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun checkBlockingBetweenLovers(initiatorId: Long, respondentId: Long) {
        if (relationRepository.existsBySourceIdAndTargetIdAndStatus(
                initiatorId,
                respondentId,
                Relation.Status.BLOCKED
            )
        ) {
            logger.info { "User '$initiatorId' blocked '$respondentId'" }
        }
        if (relationRepository.existsBySourceIdAndTargetIdAndStatus(
                respondentId,
                initiatorId,
                Relation.Status.BLOCKED
            )
        ) {
            throw LoveMapException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    ErrorCode.BlockedByUser,
                    respondentId.toString(),
                    "User '$initiatorId' is blocked by '$respondentId'."
                )
            )
        }
    }

    suspend fun setPartnershipBetween(user1: Long, user2: Long) {
        if (user1 == user2) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    user1.toString(),
                    "Source and Target in a relation cannot be the same. '$user1'"
                )
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

    suspend fun removePartnershipBetween(loverId: Long, partnerLoverId: Long) {
        relationRepository.deleteBySourceIdAndTargetId(loverId, partnerLoverId)
        relationRepository.deleteBySourceIdAndTargetId(partnerLoverId, loverId)
    }

    suspend fun getRelation(fromId: Long, toId: Long): Relation {
        return relationRepository.findBySourceIdAndTargetId(fromId, toId)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.RelationNotFound,
                    toId.toString(),
                    "Relation not found from '$fromId' to '$toId'."
                )
            )
    }

    suspend fun getRelationStatusDto(fromId: Long, toId: Long): RelationStatusDto {
        return RelationStatusDto.of(relationRepository.findBySourceIdAndTargetId(fromId, toId)?.status)
    }

    suspend fun getRelationsFrom(fromId: Long): LoverRelations {
        val relationFlow = relationRepository.findBySourceIdAndStatusIn(fromId, setOf(FOLLOWING, PARTNER))
        val loverRelationFlow = relationFlow.map { value: Relation ->
            val inRelationWith = loverService.unAuthorizedGetById(value.targetId)
            LoverRelation(
                inRelationWith.toView(),
                inRelationWith.rank,
                value.status
            )
        }
        return LoverRelations(fromId, loverRelationFlow)
    }

    suspend fun checkPartnership(loverId: Long, partnerId: Long) {
        checkBlockingBetweenLovers(loverId, partnerId)
        if (!relationRepository.existsBySourceIdAndTargetIdAndStatus(loverId, partnerId, PARTNER)) {
            throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.PartnershipNotFound,
                    partnerId.toString(),
                    "No partnership between '$loverId' and '$partnerId'"
                )
            )
        }
    }
}