package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.relation.RelationService
import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.smackspot.SmackSpotService
import com.smackmap.smackmapbackend.utils.ErrorCode.NotFoundById
import com.smackmap.smackmapbackend.utils.ErrorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackService(
    private val authorizationService: AuthorizationService,
    private val relationService: RelationService,
    private val spotService: SmackSpotService,
    private val smackRepository: SmackRepository
) {
    fun findAllInvolvedSmacksFor(smackerId: Long): Flow<Smack> {
        return smackRepository.findDistinctBySmackerIdOrSmackerPartnerId(smackerId, smackerId)
    }

    suspend fun create(request: CreateSmackRequest): Smack {
        authorizationService.checkAccessFor(request.smackerId)
        spotService.checkExistence(request.smackSpotId)
        request.smackerPartnerId?.let {
            relationService.checkPartnership(request.smackerId, it)
        }
        return smackRepository.save(
            Smack(
                name = request.name,
                smackSpotId = request.smackSpotId,
                smackerId = request.smackerId,
                smackerPartnerId = request.smackerPartnerId,
                note = request.note,
            )
        )
    }

    suspend fun isSmackerOrPartnerInSmack(smackerId: Long, smack: Smack): Boolean {
        return smack.smackerId == smackerId || smack.smackerPartnerId == smackerId
    }

    suspend fun getById(smackId: Long): Smack {
        return smackRepository.findById(smackId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundById,
                    smackId.toString(),
                    "Smack not found by id '$smackId'."
                ).toJson()
            )
    }
}