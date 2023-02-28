package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.api.MultiLoverNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class MultiLoverNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.MULTI_LOVER
    }

    override fun decorate(
        initialized: NewsFeedItemResponse,
        newsFeedData: NewsFeedData
    ): NewsFeedItemResponse {
        return if (newsFeedData is MultiLoverNewsFeedData) {
            val lovers = mapLoversToResponse(newsFeedData)
            initialized.copy(
                multiLover = MultiLoverNewsFeedResponse(lovers)
            )
        } else {
            initialized
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
