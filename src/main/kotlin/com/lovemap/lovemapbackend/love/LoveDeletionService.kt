package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveDeletionService(
    private val loveService: LoveService,
    private val loverPointService: LoverPointService,
    private val authorizationService: AuthorizationService,
    private val loveConverter: LoveConverter,
    private val loveSpotReviewService: LoveSpotReviewService,
    private val loveSpotStatisticsService: LoveSpotStatisticsService
) {

    suspend fun delete(id: Long): LoveResponse {
        val love = loveService.getById(id)
        val caller = authorizationService.checkAccessFor(love)
        loveSpotReviewService.deleteReviewsByLove(love)
        loverPointService.subtractPointsForLovemakingDeleted(love)
        loveService.delete(love)
        val latestLove = loveService.findLatestLoveAtSpot(love.loveSpotId)
        val numberOfLoves = loveService.getNumberOfLovesAtSpot(love.loveSpotId)
        loveSpotStatisticsService.deleteLoveMaking(love, latestLove, numberOfLoves)
        return loveConverter.toDto(caller, love)
    }

    suspend fun deleteAllLovesAtSpot(loveSpot: LoveSpot) {
        val loves = loveService.findLovesByLoveSpotId(loveSpot.id)
        loves.forEach { love ->
            delete(love.id)
        }
    }
}