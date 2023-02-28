package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.PhotoLikeNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.PhotoLikeNewsFeedResponse
import org.springframework.stereotype.Component

@Component
class PhotoLikeNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.LOVE_SPOT_PHOTO_LIKE
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
