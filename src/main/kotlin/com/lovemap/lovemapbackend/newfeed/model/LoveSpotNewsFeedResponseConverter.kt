package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveSpotNewsFeedResponseConverter : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is LoveSpotNewsFeedData) {
            initializedResponse.copy(
                loveSpot = LoveSpotNewsFeedResponse(
                    id = newsFeedData.id,
                    createdAt = newsFeedData.createdAt,
                    addedBy = newsFeedData.addedBy,
                    name = newsFeedData.name,
                    description = newsFeedData.description,
                    type = newsFeedData.type,
                    country = newsFeedData.country
                )
            )
        } else {
            initializedResponse
        }
    }

}
