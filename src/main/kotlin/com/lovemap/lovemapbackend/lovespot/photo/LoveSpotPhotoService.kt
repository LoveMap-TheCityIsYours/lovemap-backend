package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.photo.converter.LoveSpotPhotoConverter
import com.lovemap.lovemapbackend.lovespot.photo.converter.PhotoDto
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
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

    suspend fun getPhoto(loveSpotId: Long, photoId: Long): LoveSpotPhoto {
        return repository.findById(photoId)?.let { photo ->
            if (photo.loveSpotId != loveSpotId) {
                throw LoveMapException(HttpStatus.NOT_FOUND, ErrorCode.PhotoNotFound)
            }
            photo
        } ?: throw LoveMapException(HttpStatus.NOT_FOUND, ErrorCode.PhotoNotFound)
    }

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
        val photoDtoList = fileParts
            .map { filePart -> converter.toPhotoDto(filePart) }
            .toList()
        if (photoDtoList.any { it.byteArray.isEmpty() }) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.UploadedPhotoFileEmpty)
        }
        return photoDtoList
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
        return repository.findAllByLoveSpotIdOrderByLikesDesc(loveSpotId)
            .map { converter.toPhotoResponse(it) }
            .toList()
    }

    suspend fun getPhotosForReview(loveSpotId: Long, reviewId: Long): List<LoveSpotPhotoResponse> {
        return repository.findAllByLoveSpotIdAndLoveSpotReviewIdOrderByLikesDesc(loveSpotId, reviewId)
            .map { converter.toPhotoResponse(it) }
            .toList()
    }

    suspend fun detachPhotosFromReviews(reviews: List<LoveSpotReview>) {
        reviews.forEach { review ->
            repository.findAllByLoveSpotIdAndLoveSpotReviewId(review.loveSpotId, review.id)
                .collect { photo -> repository.save(photo.copy(loveSpotReviewId = null)) }
        }
    }

    suspend fun incrementPhotoLikes(photo: LoveSpotPhoto): LoveSpotPhoto {
        photo.likes = photo.likes + 1
        return repository.save(photo)
    }

    suspend fun incrementPhotoDislikes(photo: LoveSpotPhoto): LoveSpotPhoto {
        photo.dislikes = photo.dislikes + 1
        return repository.save(photo)
    }

    suspend fun decrementPhotoLikes(photo: LoveSpotPhoto): LoveSpotPhoto {
        photo.likes = photo.likes - 1
        return repository.save(photo)
    }

    suspend fun decrementPhotoDislikes(photo: LoveSpotPhoto): LoveSpotPhoto {
        photo.dislikes = photo.dislikes - 1
        return repository.save(photo)
    }

    fun getPhotosFrom(generateFrom: Instant): Flow<LoveSpotPhoto> {
        return repository.findAllAfterUploadedAt(Timestamp.from(generateFrom))
    }

}
