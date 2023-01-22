package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.LoveNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoveNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.stereotype.Component

@Component
class LoveNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.LOVE
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
