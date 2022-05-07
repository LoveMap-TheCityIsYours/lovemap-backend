package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.relation.RelationService
import com.lovemap.lovemapbackend.security.AuthorizationService
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.utils.ErrorCode.NotFoundById
import com.lovemap.lovemapbackend.utils.ErrorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class LoveService(
    private val authorizationService: AuthorizationService,
    private val relationService: RelationService,
    private val spotService: LoveSpotService,
    private val loveRepository: LoveRepository
) {
    fun findAllInvolvedLovesFor(loverId: Long): Flow<Love> {
        return loveRepository.findDistinctByLoverIdOrLoverPartnerId(loverId, loverId)
    }

    suspend fun create(request: CreateLoveRequest): Love {
        authorizationService.checkAccessFor(request.loverId)
        spotService.checkExistence(request.loveSpotId)
        request.loverPartnerId?.let {
            relationService.checkPartnership(request.loverId, it)
        }
        return loveRepository.save(
            Love(
                name = request.name,
                loveSpotId = request.loveSpotId,
                loverId = request.loverId,
                loverPartnerId = request.loverPartnerId,
                note = request.note,
            )
        )
    }

    suspend fun isLoverOrPartnerInLove(loverId: Long, love: Love): Boolean {
        return love.loverId == loverId || love.loverPartnerId == loverId
    }

    suspend fun getById(loveId: Long): Love {
        return loveRepository.findById(loveId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundById,
                    loveId.toString(),
                    "Love not found by id '$loveId'."
                ).toJson()
            )
    }
}