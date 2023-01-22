package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.MultiLoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.stereotype.Component

@Component
class MultiLoverNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.MULTI_LOVER
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is MultiLoverNewsFeedData) {
            val lovers = mapLoversToResponse(newsFeedData)
            initializedResponse.copy(
                multiLover = MultiLoverNewsFeedResponse(lovers)
            )
        } else {
            initializedResponse
        }
    }

    private fun mapLoversToResponse(newsFeedData: MultiLoverNewsFeedData): List<LoverNewsFeedResponse> {
        val lovers = newsFeedData.lovers.map {
            LoverNewsFeedResponse(
                id = it.id,
                userName = it.userName,
                displayName = it.userName,
                publicProfile = it.publicProfile,
                joinedAt = it.joinedAt,
                rank = it.rank,
                points = it.points,
                uuid = it.uuid
            )
        }
        return lovers
    }

}
