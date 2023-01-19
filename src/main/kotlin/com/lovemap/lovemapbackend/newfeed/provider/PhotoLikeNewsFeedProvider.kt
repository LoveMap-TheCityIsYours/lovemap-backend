package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLike
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLikeService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
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
    private val photoService: LoveSpotPhotoService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val cachedLoverService: CachedLoverService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for PhotoLikes from $generateFrom" }
        val photoLikes = photoLikeService.getPhotoLikesFrom(generateFrom)
        return photoLikes.map {
            NewsFeedItemDto(
                type = NewsFeedItemDto.Type.LOVE_SPOT_PHOTO_LIKE,
                generatedAt = generationTime,
                referenceId = it.id,
                country = cachedLoveSpotService.getCountryByLoveSpotId(it.loveSpotId),
                publicLover = cachedLoverService.getIfProfileIsPublic(it.loverId),
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
            url = photoService.getCachedPhoto(photoLike.photoId)?.url ?: NewsFeedItem.MISSING_PHOTO_URL,
            likeOrDislike = photoLike.likeOrDislike
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE
    }
}
