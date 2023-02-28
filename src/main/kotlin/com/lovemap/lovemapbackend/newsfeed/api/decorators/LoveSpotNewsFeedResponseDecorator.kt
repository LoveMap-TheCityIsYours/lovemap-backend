package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoveSpotNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.LoveSpotNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class LoveSpotNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.LOVE_SPOT
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
                    type = newsFeedData.type
                )
            )
        } else {
            initializedResponse
        }
    }

}
