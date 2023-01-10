package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveSpotPhotoNewsFeedResponseConverter : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO
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
