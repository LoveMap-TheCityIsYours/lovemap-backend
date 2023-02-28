package com.lovemap.lovemapbackend.newsfeed.model.response.decorators.processed

import com.lovemap.lovemapbackend.newsfeed.model.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.MultiLoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.decorators.ProcessedNewsFeedDataResponseDecorator
import org.springframework.stereotype.Component

@Component
class MultiLoverNewsFeedResponseDecorator : ProcessedNewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.MULTI_LOVER
    }

    override fun decorate(
        initializedResponse: NewsFeedItemResponse,
        newsFeedData: ProcessedNewsFeedData
    ): NewsFeedItemResponse {
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
