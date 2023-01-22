package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.LoveSpotReviewNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.LoveSpotReviewNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.stereotype.Component

@Component
class LoveSpotReviewNewsFeedResponseDecorator : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.LOVE_SPOT_REVIEW
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is LoveSpotReviewNewsFeedData) {
            initializedResponse.copy(
                loveSpotReview = LoveSpotReviewNewsFeedResponse(
                    id = newsFeedData.id,
                    loveSpotId = newsFeedData.loveSpotId,
                    reviewerId = newsFeedData.reviewerId,
                    submittedAt = newsFeedData.submittedAt,
                    reviewText = newsFeedData.reviewText,
                    reviewStars = newsFeedData.reviewStars,
                    riskLevel = newsFeedData.riskLevel
                )
            )
        } else {
            initializedResponse
        }
    }

}
