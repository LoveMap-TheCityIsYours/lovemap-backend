package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<LoveNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE
    }

    override fun convert(dto: NewsFeedItemDto): LoveNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is LoveNewsFeedData) {
            LoveNewsFeedResponse(
                id = newsFeedData.id,
                name = newsFeedData.name,
                loveSpotId = newsFeedData.loveSpotId,
                loverId = newsFeedData.loverId,
                happenedAt = newsFeedData.happenedAt,
                loverPartnerId = newsFeedData.loverPartnerId
            )
        } else {
            null
        }
    }

}
