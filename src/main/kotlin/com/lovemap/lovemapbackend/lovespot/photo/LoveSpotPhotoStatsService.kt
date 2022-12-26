package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import kotlinx.coroutines.Deferred
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class LoveSpotPhotoStatsService(
    private val asyncTaskService: AsyncTaskService,
    private val repository: LoveSpotPhotoRepository,
    private val loveSpotStatisticsService: LoveSpotStatisticsService,
    private val loverPointService: LoverPointService,
    private val loveSpotReviewService: LoveSpotReviewService
) {
    private val logger = KotlinLogging.logger {}

    suspend fun awaitAllAndUpdateStats(
        deferredList: List<Deferred<LoveSpotPhoto>>,
        caller: Lover,
        loveSpotId: Long,
        reviewId: Long?
    ) {
        asyncTaskService.runAsync {
            deferredList.map { it.await() }
            logger.info { "Updating statistics for newly added photos." }
            val photoCount = repository.countByLoveSpotId(loveSpotId).toInt()
            loveSpotStatisticsService.updatePhotoCounter(loveSpotId, photoCount)
            loverPointService.addPointsForPhotosAdded(caller.id, photoCount)
            reviewId?.let {
                val reviewPhotoCount = repository.countByLoveSpotReviewId(reviewId).toInt()
                loveSpotReviewService.updatePhotoCounter(reviewId, reviewPhotoCount)
            }
        }
    }
}