package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.relation.RelationService
import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.smack.location.SmackLocationService
import com.smackmap.smackmapbackend.utils.ErrorCode
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
    private val locationService: SmackLocationService,
    private val smackRepository: SmackRepository
) {
    fun findAllInvolvedSmacksFor(smackerId: Long): Flow<Smack> {
        return smackRepository.findDistinctBySmackerIdOrSmackerPartnerId(smackerId, smackerId)
    }

    suspend fun create(request: CreateSmackRequest): Smack {
        authorizationService.checkAccessFor(request.smackerId)
        locationService.checkExistence(request.smackLocationId)
        request.smackerPartnerId?.let {
            relationService.checkPartnership(request.smackerId, it)
        }
        return smackRepository.save(
            Smack(
                name = request.name,
                smackLocationId = request.smackLocationId,
                smackerId = request.smackerId,
                smackerPartnerId = request.smackerPartnerId
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