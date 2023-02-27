package com.lovemap.lovemapbackend.lovespot.photo.like

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.ranking.LoverPointService
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhoto
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoResponse
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.lovespot.photo.converter.LoveSpotPhotoConverter
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLike.Companion.DISLIKE
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLike.Companion.LIKE
import com.lovemap.lovemapbackend.newsfeed.NewsFeedDeletionService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.notification.NotificationService
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant

@Service
class PhotoLikeService(
    private val authorizationService: AuthorizationService,
    private val photoService: LoveSpotPhotoService,
    private val converter: LoveSpotPhotoConverter,
    private val loverPointService: LoverPointService,
    private val newsFeedDeletionService: NewsFeedDeletionService,
    private val notificationService: NotificationService,
    private val photoLikeRepository: PhotoLikeRepository,
    private val photoLikersDislikersRepository: PhotoLikersDislikersRepository
) {

    @Transactional
    suspend fun likePhoto(loveSpotId: Long, photoId: Long): LoveSpotPhotoResponse {
        val lover = authorizationService.getCaller()
        val (loveSpotPhoto, photoLike) = saveLikeOrDislike(lover, loveSpotId, photoId, LIKE)
        val photoLikersDislikers = addToLikersOrDislikers(photoId, photoLike, lover)
        return converter.toPhotoResponse(loveSpotPhoto, photoLikersDislikers)
    }

    @Transactional
    suspend fun dislikePhoto(loveSpotId: Long, photoId: Long): LoveSpotPhotoResponse {
        val lover = authorizationService.getCaller()
        val (loveSpotPhoto, photoLike) = saveLikeOrDislike(lover, loveSpotId, photoId, DISLIKE)
        val photoLikersDislikers = addToLikersOrDislikers(photoId, photoLike, lover)
        return converter.toPhotoResponse(loveSpotPhoto, photoLikersDislikers)
    }

    private suspend fun saveLikeOrDislike(
        lover: Lover, loveSpotId: Long, photoId: Long, likeOrDislike: Int
    ): Pair<LoveSpotPhoto, PhotoLike?> {
        val photo = photoService.getPhoto(loveSpotId, photoId)
        var photoLike: PhotoLike? = photoLikeRepository.getByPhotoIdAndLoverId(photoId, lover.id)
        photoLike = if (photoLike != null) {
            modifyLikeOrDislike(photoLike, likeOrDislike, photo, lover)
        } else {
            newLikeOrDislike(photoId, lover, likeOrDislike, photo)
        }
        if (photoLike != null) {
            photoLikeRepository.save(photoLike)
        }
        return Pair(photo, photoLike)
    }

    private suspend fun modifyLikeOrDislike(
        photoLike: PhotoLike, likeOrDislike: Int, photo: LoveSpotPhoto, lover: Lover
    ): PhotoLike? {
        return if (isChangeLikeToDislike(likeOrDislike, photoLike)) {
            handleChangeLikeToDislike(photo, photoLike, likeOrDislike, lover)
        } else if (isChangeDislikeToLike(likeOrDislike, photoLike)) {
            handleChangeDislikeToLike(photo, photoLike, likeOrDislike, lover)
        } else if (isUnlikeOrUndislike(photoLike, likeOrDislike)) {
            handleUnlikeOrUndislike(likeOrDislike, photo, photoLike, lover)
        } else {
            null
        }
    }

    private suspend fun handleChangeLikeToDislike(
        photo: LoveSpotPhoto,
        photoLike: PhotoLike,
        likeOrDislike: Int,
        lover: Lover
    ): PhotoLike {
        val disliked = photoService.decrementPhotoLikes(photo)
        photoService.incrementPhotoDislikes(disliked)
        loverPointService.subtractPointsForLikeChangeToDislike(photo, lover)
        notificationService.sendPhotoLikeNotification(photo, DISLIKE)
        photoLike.likeOrDislike = likeOrDislike
        photoLike.happenedAt = Timestamp.from(Instant.now())
        return photoLike
    }

    private suspend fun handleChangeDislikeToLike(
        photo: LoveSpotPhoto,
        photoLike: PhotoLike,
        likeOrDislike: Int,
        lover: Lover
    ): PhotoLike {
        val liked = photoService.incrementPhotoLikes(photo)
        photoService.decrementPhotoDislikes(liked)
        loverPointService.addPointsForDislikeChangeToLike(photo, lover)
        notificationService.sendPhotoLikeNotification(photo, LIKE)
        photoLike.likeOrDislike = likeOrDislike
        photoLike.happenedAt = Timestamp.from(Instant.now())
        return photoLike
    }

    private suspend fun handleUnlikeOrUndislike(
        likeOrDislike: Int,
        photo: LoveSpotPhoto,
        photoLike: PhotoLike,
        lover: Lover
    ): Nothing? {
        if (likeOrDislike == LIKE) {
            photoService.decrementPhotoLikes(photo)
            loverPointService.subtractPointsForUnlike(photo, lover)
        } else if (likeOrDislike == DISLIKE) {
            photoService.decrementPhotoDislikes(photo)
            loverPointService.addPointsForUndislike(photo, lover)
        }
        newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE, photoLike.id)
        photoLikeRepository.delete(photoLike)
        return null
    }

    private fun isChangeLikeToDislike(
        likeOrDislike: Int, photoLike: PhotoLike
    ) = likeOrDislike == DISLIKE && photoLike.likeOrDislike == LIKE

    private fun isChangeDislikeToLike(
        likeOrDislike: Int, photoLike: PhotoLike
    ) = likeOrDislike == LIKE && photoLike.likeOrDislike == DISLIKE

    private fun isUnlikeOrUndislike(
        photoLike: PhotoLike, likeOrDislike: Int
    ) = likeOrDislike == LIKE && photoLike.likeOrDislike == LIKE
            || likeOrDislike == DISLIKE && photoLike.likeOrDislike == DISLIKE

    private suspend fun newLikeOrDislike(
        photoId: Long, lover: Lover, likeOrDislike: Int, photo: LoveSpotPhoto
    ): PhotoLike {
        val photoLike = PhotoLike(
            photoId = photoId,
            loverId = lover.id,
            likeOrDislike = likeOrDislike,
            happenedAt = Timestamp.from(Instant.now()),
            loveSpotId = photo.loveSpotId
        )
        if (likeOrDislike == LIKE) {
            photoService.incrementPhotoLikes(photo)
            loverPointService.addPointsForLike(photo, lover)
        } else if (likeOrDislike == DISLIKE) {
            photoService.incrementPhotoDislikes(photo)
            loverPointService.subtractPointsForDislike(photo, lover)
        }
        notificationService.sendPhotoLikeNotification(photo, likeOrDislike)
        return photoLike
    }

    private suspend fun addToLikersOrDislikers(
        photoId: Long, photoLike: PhotoLike?, lover: Lover
    ): PhotoLikersDislikers {
        val photoLikersDislikers = photoLikersDislikersRepository.findByPhotoId(photoId)?.let { photoLikersDislikers ->
            modifyLikersDislikers(photoLike, photoLikersDislikers, lover)
        } ?: newLikersDislikers(photoId, photoLike, lover)
        return photoLikersDislikersRepository.save(photoLikersDislikers)
    }

    private fun modifyLikersDislikers(
        photoLike: PhotoLike?, photoLikersDislikers: PhotoLikersDislikers, lover: Lover
    ): PhotoLikersDislikers {
        if (photoLike == null) {
            photoLikersDislikers.removeLiker(lover.id)
            photoLikersDislikers.removeDisliker(lover.id)
        } else {
            if (photoLike.likeOrDislike == LIKE) {
                photoLikersDislikers.addLiker(lover.id)
            } else if (photoLike.likeOrDislike == DISLIKE) {
                photoLikersDislikers.addDisliker(lover.id)
            }
        }
        return photoLikersDislikers
    }

    private fun newLikersDislikers(
        photoId: Long, photoLike: PhotoLike?, lover: Lover
    ): PhotoLikersDislikers {
        return if (photoLike == null) {
            PhotoLikersDislikers(
                photoId = photoId
            )
        } else {
            PhotoLikersDislikers(
                photoId = photoId
            ).apply {
                if (photoLike.likeOrDislike == LIKE) {
                    addLiker(lover.id)
                } else if (photoLike.likeOrDislike == DISLIKE) {
                    addDisliker(lover.id)
                }
            }
        }
    }

    suspend fun deletePhotoLikes(photo: LoveSpotPhoto) {
        photoLikeRepository.findByPhotoId(photo.id).collect { photoLike ->
            newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE, photoLike.id)
        }
        photoLikeRepository.deleteByPhotoId(photo.id)
        photoLikersDislikersRepository.deleteByPhotoId(photo.id)
    }

    fun getPhotoLikesFrom(generationTime: Instant): Flow<PhotoLike> {
        return photoLikeRepository.findAllAfterHappenedAt(Timestamp.from(generationTime))
    }
}
