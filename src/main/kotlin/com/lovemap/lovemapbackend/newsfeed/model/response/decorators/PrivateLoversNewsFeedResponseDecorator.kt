package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.PrivateLoversNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.response.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.PrivateLoversNewsFeedResponse
import org.springframework.stereotype.Component

@Component
class PrivateLoversNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.PRIVATE_LOVERS
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is PrivateLoversNewsFeedData) {
            val lovers = mapLoversToResponse(newsFeedData)
            initializedResponse.copy(
                privateLovers = PrivateLoversNewsFeedResponse(lovers)
            )
        } else {
            initializedResponse
        }
    }

    private fun mapLoversToResponse(newsFeedData: PrivateLoversNewsFeedData): List<LoverNewsFeedResponse> {
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
