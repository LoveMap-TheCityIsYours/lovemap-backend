package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveSpotNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<LoveSpotNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT
    }

    override fun convert(dto: NewsFeedItemDto): LoveSpotNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is LoveSpotNewsFeedData) {
            LoveSpotNewsFeedResponse(
                id = newsFeedData.id,
                createdAt = newsFeedData.createdAt,
                addedBy = newsFeedData.addedBy,
                name = newsFeedData.name,
                description = newsFeedData.description,
                type = newsFeedData.type,
                country = newsFeedData.country
            )
        } else {
            null
        }
    }

}
