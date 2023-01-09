package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLikeService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.newfeed.NewsFeedDeletionService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PhotoDeletionService(
    private val repository: LoveSpotPhotoRepository,
    private val loveSpotService: LoveSpotService,
    private val asyncTaskService: AsyncTaskService,
    private val photoStore: PhotoStore,
    private val photoService: LoveSpotPhotoService,
    private val authorizationService: AuthorizationService,
    private val loveSpotReviewService: LoveSpotReviewService,
    private val photoLikeService: PhotoLikeService,
    private val loverPointService: LoverPointService,
    private val newsFeedDeletionService: NewsFeedDeletionService
) {

    suspend fun deletePhotosForLoveSpot(loveSpot: LoveSpot) {
        repository.findAllByLoveSpotId(loveSpot.id).collect { photo ->
            newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.LOVE_SPOT_PHOTO, photo.id)
            photoLikeService.deletePhotoLikes(photo)
            loverPointService.subtractPointsForPhotoDeleted(photo)
            repository.delete(photo)
            asyncTaskService.runAsync {
                photoStore.delete(photo)
            }
        }
    }

    @Transactional
    suspend fun deletePhoto(loveSpotId: Long, photoId: Long): List<LoveSpotPhotoResponse> {
        return repository.findById(photoId)?.let { photo ->
            authorizeDeletion(photo, loveSpotId)
            photoLikeService.deletePhotoLikes(photo)
            loverPointService.subtractPointsForPhotoDeleted(photo)
            repository.delete(photo)
            loveSpotService.decrementNumberOfPhotos(loveSpotId)
            asyncTaskService.runAsync {
                photoStore.delete(photo)
            }
            photoService.getPhotosForSpot(loveSpotId)
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