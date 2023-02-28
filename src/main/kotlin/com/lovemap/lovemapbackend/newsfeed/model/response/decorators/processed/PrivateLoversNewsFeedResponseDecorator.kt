package com.lovemap.lovemapbackend.newsfeed.model.response.decorators.processed

import com.lovemap.lovemapbackend.newsfeed.model.PrivateLoversNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.PrivateLoversNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.decorators.ProcessedNewsFeedDataResponseDecorator
import org.springframework.stereotype.Component

@Component
class PrivateLoversNewsFeedResponseDecorator : ProcessedNewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.PRIVATE_LOVERS
    }

    override fun decorate(
        initializedResponse: NewsFeedItemResponse,
        newsFeedData: ProcessedNewsFeedData
    ): NewsFeedItemResponse {
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
