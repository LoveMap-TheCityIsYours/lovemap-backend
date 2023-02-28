package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.api.PrivateLoversNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.PrivateLoversNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class PrivateLoversNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.PRIVATE_LOVERS
    }

    override fun decorate(
        initialized: NewsFeedItemResponse,
        newsFeedData: NewsFeedData
    ): NewsFeedItemResponse {
        return if (newsFeedData is PrivateLoversNewsFeedData) {
            val lovers = mapLoversToResponse(newsFeedData)
            initialized.copy(
                privateLovers = PrivateLoversNewsFeedResponse(lovers)
            )
        } else {
            initialized
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
