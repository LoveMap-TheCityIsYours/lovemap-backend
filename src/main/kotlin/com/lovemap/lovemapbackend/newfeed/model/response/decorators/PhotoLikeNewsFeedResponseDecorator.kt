package com.lovemap.lovemapbackend.newfeed.model.response.decorators

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.PhotoLikeNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newfeed.model.response.PhotoLikeNewsFeedResponse
import org.springframework.stereotype.Component

@Component
class PhotoLikeNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

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
