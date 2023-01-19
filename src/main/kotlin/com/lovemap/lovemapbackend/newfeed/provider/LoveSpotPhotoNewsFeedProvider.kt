package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhoto
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoveSpotPhotoNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoveSpotPhotoNewsFeedProvider(
    private val photoService: LoveSpotPhotoService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val cachedLoverService: CachedLoverService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for LoveSpot Photos from $generateFrom" }
        val photos = photoService.getPhotosFrom(generateFrom)
        return photos.map {
            NewsFeedItemDto(
                type = NewsFeedItemDto.Type.LOVE_SPOT_PHOTO,
                generatedAt = generationTime,
                referenceId = it.id,
                country = cachedLoveSpotService.getCountryByLoveSpotId(it.loveSpotId),
                publicLover = cachedLoverService.getIfProfileIsPublic(it.uploadedBy),
                newsFeedData = photoToNewsFeedData(it)
            )
        }
    }

    private suspend fun photoToNewsFeedData(loveSpotPhoto: LoveSpotPhoto): LoveSpotPhotoNewsFeedData {
        return LoveSpotPhotoNewsFeedData(
            id = loveSpotPhoto.id,
            loveSpotId = loveSpotPhoto.loveSpotId,
            uploadedBy = loveSpotPhoto.uploadedBy,
            uploadedAt = loveSpotPhoto.uploadedAt.toInstant(),
            fileName = loveSpotPhoto.fileName,
            url = loveSpotPhoto.url,
            loveSpotReviewId = loveSpotPhoto.loveSpotReviewId,
            likes = loveSpotPhoto.likes,
            dislikes = loveSpotPhoto.dislikes,
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO
    }
}
