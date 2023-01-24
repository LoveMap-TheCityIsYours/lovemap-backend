package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.ranking.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.newsfeed.NewsFeedDeletionService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
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
    private val loveSpotPhotoService: LoveSpotPhotoService,
    private val loveSpotStatisticsService: LoveSpotStatisticsService,
    private val newsFeedDeletionService: NewsFeedDeletionService
) {

    suspend fun delete(id: Long): LoveResponse {
        val love = loveService.getById(id)
        val caller = authorizationService.checkAccessFor(love)
        val reviewsByLove = loveSpotReviewService.getReviewsByLove(love)
        loveSpotPhotoService.detachPhotosFromReviews(reviewsByLove)
        loveSpotReviewService.deleteReviewsByLove(love)
        loverPointService.subtractPointsForLovemakingDeleted(love)
        loveService.delete(love)
        val latestLove = loveService.findLatestLoveAtSpot(love.loveSpotId)
        val numberOfLoves = loveService.getNumberOfLovesAtSpot(love.loveSpotId)
        loveSpotStatisticsService.deleteLoveMaking(love, latestLove, numberOfLoves)
        newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.LOVE, id)
        return loveConverter.toDto(caller, love)
    }

    suspend fun deleteAllLovesAtSpot(loveSpot: LoveSpot) {
        val loves = loveService.findLovesByLoveSpotId(loveSpot.id)
        loves.forEach { love ->
            delete(love.id)
        }
    }
}