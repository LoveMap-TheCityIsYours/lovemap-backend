package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

@Service
class LoveSpotPhotoService(
    private val converter: LoveSpotPhotoConverter,
    private val asyncTaskService: AsyncTaskService,
    private val authorizationService: AuthorizationService,
    private val loveSpotStatisticsService: LoveSpotStatisticsService,
    private val loveSpotService: LoveSpotService,
    private val loveSpotReviewService: LoveSpotReviewService,
    private val loverPointService: LoverPointService,
    private val repository: LoveSpotPhotoRepository,
    private val photoStore: PhotoStore
) {
    private val logger = KotlinLogging.logger {}

    suspend fun uploadToSpot(loveSpotId: Long, fileParts: Flow<FilePart>) {
        val caller = authorizationService.getCaller()
        loveSpotService.authorizedGetById(loveSpotId)
        val photos: List<PhotoDto> = collectPhotoObjects(fileParts)
        val deferredList: List<Deferred<LoveSpotPhoto>> = persistAllAsync(photos, loveSpotId, null, caller)
        awaitAllAndUpdateStats(deferredList, caller, loveSpotId, null)
    }

    suspend fun uploadToReview(loveSpotId: Long, reviewId: Long, fileParts: Flow<FilePart>) {
        val caller = authorizationService.getCaller()
        loveSpotReviewService.authorizedGetById(loveSpotId, reviewId)
        val photos: List<PhotoDto> = collectPhotoObjects(fileParts)
        val deferredList: List<Deferred<LoveSpotPhoto>> = persistAllAsync(photos, loveSpotId, reviewId, caller)
        awaitAllAndUpdateStats(deferredList, caller, loveSpotId, reviewId)
    }

    private suspend fun collectPhotoObjects(fileParts: Flow<FilePart>): List<PhotoDto> {
        return fileParts
            .map { filePart -> converter.toPhotoDto(filePart) }
            .toList()
    }

    private suspend fun persistAllAsync(
        photos: List<PhotoDto>,
        loveSpotId: Long,
        reviewId: Long?,
        caller: Lover
    ): List<Deferred<LoveSpotPhoto>> {
        val deferredList: List<Deferred<LoveSpotPhoto>> = photos.map { photoDto ->
            asyncTaskService.runAsync {
                val convertedPhoto = converter.convertEncoding(photoDto)
                val url = photoStore.persist(convertedPhoto)
                repository.save(
                    LoveSpotPhoto(
                        url = url,
                        loveSpotId = loveSpotId,
                        loveSpotReviewId = reviewId,
                        uploadedBy = caller.id
                    )
                )
            }
        }
        return deferredList
    }

    private suspend fun awaitAllAndUpdateStats(
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

    suspend fun getPhotosForSpot(loveSpotId: Long): List<LoveSpotPhotoResponse> {
        return repository.findAllByLoveSpotId(loveSpotId)
            .map {
                LoveSpotPhotoResponse(
                    loveSpotId = it.loveSpotId,
                    reviewId = it.loveSpotReviewId,
                    likes = it.likes,
                    dislikes = it.dislikes,
                    url = it.url
                )
            }
            .toList()
    }

    suspend fun getPhotosForReview(loveSpotId: Long, reviewId: Long): List<LoveSpotPhotoResponse> {
        return repository.findAllByLoveSpotIdAndLoveSpotReviewId(loveSpotId, reviewId)
            .map {
                LoveSpotPhotoResponse(
                    loveSpotId = it.loveSpotId,
                    reviewId = it.loveSpotReviewId,
                    likes = it.likes,
                    dislikes = it.dislikes,
                    url = it.url
                )
            }
            .toList()
    }
}