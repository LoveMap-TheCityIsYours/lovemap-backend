package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.api.PhotoLikeNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.PhotoLikeNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class PhotoLikeNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.LOVE_SPOT_PHOTO_LIKE
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is PhotoLikeNewsFeedData) {
            initializedResponse.copy(
                photoLike = PhotoLikeNewsFeedResponse(
                    id = newsFeedData.id,
                    loveSpotId = newsFeedData.loveSpotId,
                    loveSpotPhotoId = newsFeedData.loveSpotPhotoId,
                    url = newsFeedData.url ?: NewsFeedItem.MISSING_PHOTO_URL,
                    happenedAt = newsFeedData.happenedAt,
                    loverId = newsFeedData.loverId,
                    likeOrDislike = newsFeedData.likeOrDislike,
                )
            )
        } else {
            initializedResponse
        }
    }

}
