package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class PhotoLikeNewsFeedResponseConverter : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is PhotoLikeNewsFeedData) {
            initializedResponse.copy(
                photoLike = PhotoLikeNewsFeedResponse(
                    id = newsFeedData.id,
                    loveSpotId = newsFeedData.loveSpotId,
                    loveSpotPhotoId = newsFeedData.loveSpotPhotoId,
                    happenedAt = newsFeedData.happenedAt,
                    loverId = newsFeedData.loverId,
                    likeOrDislike = newsFeedData.likeOrDislike
                )
            )
        } else {
            initializedResponse
        }
    }

}
