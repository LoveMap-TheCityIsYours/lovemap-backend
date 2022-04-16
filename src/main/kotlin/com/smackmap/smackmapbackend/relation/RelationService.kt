package com.smackmap.smackmapbackend.relation

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
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
}