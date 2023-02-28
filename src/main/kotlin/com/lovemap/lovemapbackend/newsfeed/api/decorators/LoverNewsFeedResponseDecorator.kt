package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.LoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class LoverNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.LOVER
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is LoverNewsFeedData) {
            initializedResponse.copy(
                lover = LoverNewsFeedResponse(
                    id = newsFeedData.id,
                    userName = newsFeedData.userName,
                    displayName = newsFeedData.userName,
                    publicProfile = newsFeedData.publicProfile,
                    joinedAt = newsFeedData.joinedAt,
                    rank = newsFeedData.rank,
                    points = newsFeedData.points,
                    uuid = newsFeedData.uuid
                )
            )
        } else {
            initializedResponse
        }
    }

}
