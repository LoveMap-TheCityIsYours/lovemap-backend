package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.ranking.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.notification.NotificationService
import com.lovemap.lovemapbackend.notification.NotificationType.NEW_LOVE_SPOT_PHOTO
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
    private val notificationService: NotificationService,
    private val loveSpotReviewService: LoveSpotReviewService
) {
    private val logger = KotlinLogging.logger {}

    suspend fun awaitAllAndUpdateStats(
        deferredList: Deferred<List<LoveSpotPhoto>>,
        caller: Lover,
        loveSpot: LoveSpot,
        reviewId: Long?
    ) {
        asyncTaskService.runAsync {
            runCatching {
                val uploadedPhotos: List<LoveSpotPhoto> = deferredList.await()
                logger.info { "Updating statistics for newly added photos." }
                val photoCount = uploadedPhotos.size
                loveSpotStatisticsService.updatePhotoStats(loveSpot, photoCount)
                loverPointService.addPointsForPhotosAdded(caller.id, photoCount)
                reviewId?.let {
                    val reviewPhotoCount = repository.countByLoveSpotReviewId(reviewId).toInt()
                    loveSpotReviewService.updatePhotoCounter(reviewId, reviewPhotoCount)
                }
                notificationService.sendNearbyLoveSpotNotification(loveSpot, NEW_LOVE_SPOT_PHOTO)
            }.onFailure { e ->
                logger.error(e) { "Error occurred during awaiting photo upload." }
            }
        }
    }
}