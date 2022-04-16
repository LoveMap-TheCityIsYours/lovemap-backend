package com.smackmap.smackmapbackend.relation

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class RelationService(
    private val relationRepository: RelationRepository
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
                "User '$initiatorId' blocked '$respondentId', first need to unblock."
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
                "User '$initiatorId' is blocked by '$respondentId'."
            )
        }
    }

    suspend fun updateRelations(user1: Long, user2: Long, status: Relation.Status) {
        if (user1 == user2) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Source and Target in a relation cannot be the same. '$user1'")
        }
        val relation12: Relation = relationRepository.findBySourceIdAndTargetId(user1, user2)
            ?: Relation(status = status, sourceId = user1, targetId = user2)
        relation12.status = status

        val relation21: Relation = relationRepository.findBySourceIdAndTargetId(user2, user1)
            ?: Relation(status = status, sourceId = user2, targetId = user1)
        relation21.status = status
    }
}