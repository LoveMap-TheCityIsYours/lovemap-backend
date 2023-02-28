package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.LoveSpotReviewNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.LoveSpotReviewNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class LoveSpotReviewNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.LOVE_SPOT_REVIEW
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
