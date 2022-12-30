package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant

@Service
class LoveSpotPhotoService(
    private val converter: LoveSpotPhotoConverter,
    private val asyncTaskService: AsyncTaskService,
    private val authorizationService: AuthorizationService,
    private val loveSpotService: LoveSpotService,
    private val loveSpotReviewService: LoveSpotReviewService,
    private val loveSpotPhotoStatsService: LoveSpotPhotoStatsService,
    private val repository: LoveSpotPhotoRepository,
    private val photoStore: PhotoStore
) {
    private val logger = KotlinLogging.logger {}

    suspend fun uploadToSpot(loveSpotId: Long, fileParts: Flow<FilePart>) {
        val caller = authorizationService.getCaller()
        loveSpotService.authorizedGetById(loveSpotId)
        val photos: List<PhotoDto> = collectPhotoObjects(fileParts)
        val deferredList: List<Deferred<LoveSpotPhoto>> = persistAllAsync(photos, loveSpotId, null, caller)
        loveSpotPhotoStatsService.awaitAllAndUpdateStats(deferredList, caller, loveSpotId, null)
    }

    suspend fun uploadToReview(loveSpotId: Long, reviewId: Long, fileParts: Flow<FilePart>) {
        val caller = authorizationService.getCaller()
        loveSpotReviewService.authorizedGetById(loveSpotId, reviewId)
        val photos: List<PhotoDto> = collectPhotoObjects(fileParts)
        val deferredList: List<Deferred<LoveSpotPhoto>> = persistAllAsync(photos, loveSpotId, reviewId, caller)
        loveSpotPhotoStatsService.awaitAllAndUpdateStats(deferredList, caller, loveSpotId, reviewId)
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
        val uploadedAt = Timestamp.from(Instant.now())
        val deferredList: List<Deferred<LoveSpotPhoto>> = photos.map { photoDto ->
            asyncTaskService.runAsync {
                val convertedPhoto = converter.convertEncoding(photoDto)
                val url = photoStore.persist(convertedPhoto)
                repository.save(
                    LoveSpotPhoto(
                        url = url,
                        loveSpotId = loveSpotId,
                        loveSpotReviewId = reviewId,
                        uploadedBy = caller.id,
                        uploadedAt = uploadedAt,
                        fileName = photoDto.fileName
                    )
                )
            }
        }
        return deferredList
    }

    suspend fun getPhotosForSpot(loveSpotId: Long): List<LoveSpotPhotoResponse> {
        return repository.findAllByLoveSpotId(loveSpotId)
            .map { LoveSpotPhotoResponse.of(it) }
            .toList()
    }

    suspend fun getPhotosForReview(loveSpotId: Long, reviewId: Long): List<LoveSpotPhotoResponse> {
        return repository.findAllByLoveSpotIdAndLoveSpotReviewId(loveSpotId, reviewId)
            .map { LoveSpotPhotoResponse.of(it) }
            .toList()
    }

    suspend fun detachPhotosFromReviews(reviews: List<LoveSpotReview>) {
        reviews.forEach { review ->
            repository.findAllByLoveSpotIdAndLoveSpotReviewId(review.loveSpotId, review.id)
                .collect { photo -> repository.save(photo.copy(loveSpotReviewId = null)) }
        }
    }

    suspend fun deletePhotosForLoveSpot(loveSpot: LoveSpot) {
        repository.findAllByLoveSpotId(loveSpot.id).collect {
            repository.delete(it)
            asyncTaskService.runAsync {
                photoStore.delete(it)
            }
        }
    }

    @Transactional
    suspend fun deletePhoto(loveSpotId: Long, photoId: Long): List<LoveSpotPhotoResponse> {
        return repository.findById(photoId)?.let { photo ->
            authorizeDeletion(photo, loveSpotId)
            repository.delete(photo)
            loveSpotService.decrementNumberOfPhotos(loveSpotId)
            asyncTaskService.runAsync {
                photoStore.delete(photo)
            }
            getPhotosForSpot(loveSpotId)
        } ?: throw LoveMapException(HttpStatus.NOT_FOUND, ErrorCode.PhotoNotFound)
    }

    private suspend fun authorizeDeletion(
        photo: LoveSpotPhoto,
        loveSpotId: Long
    ) {
        if (photo.loveSpotId != loveSpotId) {
            throw LoveMapException(HttpStatus.NOT_FOUND, ErrorCode.PhotoNotFound)
        }
        if (!authorizationService.isAdmin()) {
            if (photo.loveSpotReviewId != null) {
                loveSpotReviewService.authorizedGetById(loveSpotId, photo.loveSpotReviewId!!)
            } else {
                val loveSpot = loveSpotService.getById(loveSpotId)
                authorizationService.checkAccessFor(loveSpot)
            }
        }
    }
}
