package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.stereotype.Component

@Component
class LoverNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.LOVER
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
