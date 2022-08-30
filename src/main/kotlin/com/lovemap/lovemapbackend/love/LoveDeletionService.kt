package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
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
) {

    suspend fun delete(id: Long): LoveResponse {
        val love = loveService.getById(id)
        val caller = authorizationService.checkAccessFor(love)
        loveSpotReviewService.deleteReviewsByLove(love)
        loverPointService.subtractPointsForLovemakingDeleted(love)
        loveService.delete(love)
        return loveConverter.toDto(caller, love)
    }

    suspend fun deleteLovesBySpot(loveSpot: LoveSpot) {
        val loves = loveService.findLovesByLoveSpot(loveSpot)
        loves.forEach { love ->
            loverPointService.subtractPointsForLovemakingDeleted(love)
        }
        loveService.deleteLovesBySpot(loveSpot)
    }
}