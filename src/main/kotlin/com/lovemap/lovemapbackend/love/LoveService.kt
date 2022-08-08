package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.relation.RelationService
import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.utils.ErrorCode.NotFoundById
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.InstantConverterUtils
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant

@Service
@Transactional
class LoveService(
    private val authorizationService: AuthorizationService,
    private val loveConverter: LoveConverter,
    private val relationService: RelationService,
    private val loveSpotService: LoveSpotService,
    private val loverPointService: LoverPointService,
    private val loveRepository: LoveRepository
) {
    suspend fun findAllInvolvedLovesFor(loverId: Long): List<LoveResponse> {
        val caller = authorizationService.checkAccessFor(loverId)
        val loves = loveRepository.findDistinctByLoverIdOrLoverPartnerId(loverId, loverId)
        return loves.map { love -> loveConverter.toDto(caller, love) }.toList()
    }

    suspend fun create(request: CreateLoveRequest): LoveResponse {
        val caller = checkAndValidateLove(request)
        val happenedAt = getHappenedAt(request)
        val love = loveRepository.save(
            Love(
                name = request.name.trim(),
                loveSpotId = request.loveSpotId,
                loverId = request.loverId,
                loverPartnerId = request.loverPartnerId,
                note = request.note?.trim(),
                happenedAt = happenedAt
            )
        )
        loverPointService.addPointsForLovemaking(love)
        loveSpotService.recordLoveMaking(love)
        return loveConverter.toDto(caller, love)
    }

    private fun getHappenedAt(request: CreateLoveRequest): Timestamp {
        return (request.happenedAt?.let { Timestamp.from(InstantConverterUtils.fromString(it)) }
            ?: Timestamp.from(Instant.now()))
    }

    private suspend fun checkAndValidateLove(request: CreateLoveRequest): Lover {
        val caller = authorizationService.checkAccessFor(request.loverId)
        loveSpotService.checkExistence(request.loveSpotId)
        request.loverPartnerId?.let {
            relationService.checkPartnership(request.loverId, it)
        }
        return caller
    }

    suspend fun update(id: Long, request: UpdateLoveRequest): LoveResponse {
        var love = getById(id)
        val caller = authorizationService.checkAccessFor(love)
        request.name?.let { love.name = it.trim() }
        request.note?.let { love.note = it.trim() }
        request.happenedAt?.let {
            love.happenedAt = Timestamp.from(InstantConverterUtils.fromString(it))
        }
        request.loverPartnerId?.let {
            relationService.checkPartnership(love.loverId, it)
            love.loverPartnerId = request.loverPartnerId
        } ?: run {
            love.loverPartnerId = null
        }
        love = loveRepository.save(love)
        return loveConverter.toDto(caller, love)
    }

    suspend fun getById(loveId: Long): Love {
        val love = loveRepository.findById(loveId)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    NotFoundById,
                    loveId.toString(),
                    "Love not found by id '$loveId'."
                )
            )
        authorizationService.checkAccessFor(love)
        return love
    }

    suspend fun deleteLovesBySpot(loveSpot: LoveSpot) {
        val loves = loveRepository.findByLoveSpotId(loveSpot.id)
        loves.collect { love ->
            loverPointService.subtractPointsForLovemakingDeleted(love)
        }
        loveRepository.deleteByLoveSpotId(loveSpot.id)
    }

    suspend fun delete(love: Love) {
        loveRepository.delete(love)
    }
}