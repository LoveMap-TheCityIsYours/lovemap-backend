package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class PhotoLikeNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<PhotoLikeNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE
    }

    override fun convert(dto: NewsFeedItemDto): PhotoLikeNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is PhotoLikeNewsFeedData) {
            PhotoLikeNewsFeedResponse(
                id = newsFeedData.id,
                loveSpotId = newsFeedData.loveSpotId,
                loveSpotPhotoId = newsFeedData.loveSpotPhotoId,
                happenedAt = newsFeedData.happenedAt,
                loverId = newsFeedData.loverId,
                likeOrDislike = newsFeedData.likeOrDislike
            )
        } else {
            null
        }
    }

}
