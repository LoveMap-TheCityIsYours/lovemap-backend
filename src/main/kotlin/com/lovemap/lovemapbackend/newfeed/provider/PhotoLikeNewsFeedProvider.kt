package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLike
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLikeService
import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.PhotoLikeNewsFeedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PhotoLikeNewsFeedProvider(
    private val photoLikeService: PhotoLikeService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for Loves from $generateFrom" }
        val loves = photoLikeService.getPhotoLikesFrom(generateFrom)
        return loves.map {
            NewsFeedItemDto(
                type = NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = photoLikeToNewsFeedData(it)
            )
        }
    }

    private suspend fun photoLikeToNewsFeedData(photoLike: PhotoLike): PhotoLikeNewsFeedData {
        return PhotoLikeNewsFeedData(
            id = photoLike.id,
            loveSpotId = photoLike.loveSpotId,
            loverId = photoLike.loverId,
            happenedAt = photoLike.happenedAt.toInstant(),
            loveSpotPhotoId = photoLike.photoId,
            likeOrDislike = photoLike.likeOrDislike
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE
    }
}
