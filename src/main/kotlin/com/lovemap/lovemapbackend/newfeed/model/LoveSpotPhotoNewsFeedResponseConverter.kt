package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveSpotPhotoNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<LoveSpotPhotoNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO
    }

    override fun convert(dto: NewsFeedItemDto): LoveSpotPhotoNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is LoveSpotPhotoNewsFeedData) {
            LoveSpotPhotoNewsFeedResponse(
                id = newsFeedData.id,
                loveSpotId = newsFeedData.loveSpotId,
                uploadedBy = newsFeedData.uploadedBy,
                uploadedAt = newsFeedData.uploadedAt,
                fileName = newsFeedData.fileName,
                loveSpotReviewId = newsFeedData.loveSpotReviewId,
                likes = newsFeedData.likes,
                dislikes = newsFeedData.dislikes
            )
        } else {
            null
        }
    }

}
