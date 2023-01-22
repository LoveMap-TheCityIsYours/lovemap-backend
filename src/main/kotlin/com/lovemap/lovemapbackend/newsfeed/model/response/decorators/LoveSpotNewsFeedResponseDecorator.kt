package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.LoveSpotNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoveSpotNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.stereotype.Component

@Component
class LoveSpotNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.LOVE_SPOT
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
