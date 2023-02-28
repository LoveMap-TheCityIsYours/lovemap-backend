package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.data.LoveNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.response.LoveNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class LoveNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.LOVE
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is LoveNewsFeedData) {
            initializedResponse.copy(
                love = LoveNewsFeedResponse(
                    id = newsFeedData.id,
                    name = newsFeedData.name,
                    loveSpotId = newsFeedData.loveSpotId,
                    loverId = newsFeedData.loverId,
                    happenedAt = newsFeedData.happenedAt,
                    loverPartnerId = newsFeedData.loverPartnerId,
                    publicLoverPartner = newsFeedData.publicLoverPartner
                )
            )
        } else {
            initializedResponse
        }
    }

}
