package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.model.LoveSpotPhotoNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoveSpotPhotoNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.stereotype.Component

@Component
class LoveSpotPhotoNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.LOVE_SPOT_PHOTO
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is LoveSpotPhotoNewsFeedData) {
            initializedResponse.copy(
                loveSpotPhoto = LoveSpotPhotoNewsFeedResponse(
                    id = newsFeedData.id,
                    loveSpotId = newsFeedData.loveSpotId,
                    uploadedBy = newsFeedData.uploadedBy,
                    uploadedAt = newsFeedData.uploadedAt,
                    fileName = newsFeedData.fileName,
                    url = newsFeedData.url ?: NewsFeedItem.MISSING_PHOTO_URL,
                    loveSpotReviewId = newsFeedData.loveSpotReviewId,
                    likes = newsFeedData.likes,
                    dislikes = newsFeedData.dislikes
                )
            )
        } else {
            initializedResponse
        }
    }

}