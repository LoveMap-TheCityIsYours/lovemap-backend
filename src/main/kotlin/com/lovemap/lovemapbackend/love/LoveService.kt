package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.relation.RelationService
import com.lovemap.lovemapbackend.security.AuthorizationService
import com.lovemap.lovemapbackend.utils.ErrorCode.NotFoundById
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.InstantConverterUtils
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.time.Instant

@Service
@Transactional
class LoveService(
    private val authorizationService: AuthorizationService,
    private val relationService: RelationService,
    private val spotService: LoveSpotService,
    private val loverPointService: LoverPointService,
    private val loveRepository: LoveRepository
) {
    suspend fun findAllInvolvedLovesFor(loverId: Long): Flow<Love> {
        authorizationService.checkAccessFor(loverId)
        return loveRepository.findDistinctByLoverIdOrLoverPartnerId(loverId, loverId)
    }

    suspend fun create(request: CreateLoveRequest): Love {
        authorizationService.checkAccessFor(request.loverId)
        spotService.checkExistence(request.loveSpotId)
        request.loverPartnerId?.let {
            relationService.checkPartnership(request.loverId, it)
        }
        val love = loveRepository.save(
            Love(
                name = request.name,
                loveSpotId = request.loveSpotId,
                loverId = request.loverId,
                loverPartnerId = request.loverPartnerId,
                note = request.note,
                happenedAt = request.happenedAt?.let { Timestamp.from(InstantConverterUtils.fromString(it)) }
                    ?: Timestamp.from(Instant.now())
            )
        )
        loverPointService.addPointsForLovemaking(love)
        return love
    }

    suspend fun update(id: Long, request: UpdateLoveRequest): Love {
        val love = getById(id)
        request.name?.let { love.name = it }
        request.note?.let { love.note = it }
        request.happenedAt?.let {
            love.happenedAt = Timestamp.from(InstantConverterUtils.fromString(it))
        }
        request.loverPartnerId?.let {
            relationService.checkPartnership(love.loverId, it)
            love.loverPartnerId = request.loverPartnerId
        } ?: run {
            love.loverPartnerId = null
        }
        return loveRepository.save(love)
    }

    suspend fun getById(loveId: Long): Love {
        val love = loveRepository.findById(loveId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundById,
                    loveId.toString(),
                    "Love not found by id '$loveId'."
                ).toJson()
            )
        authorizationService.checkAccessFor(love)
        return love
    }

    suspend fun deleteLovesBySpot(loveSpotId: Long) {
        loveRepository.deleteByLoveSpotId(loveSpotId)
    }

    suspend fun delete(love: Love) {
        loveRepository.delete(love)
    }
}