package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveSpotReviewNewsFeedResponseConverter : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_REVIEW
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
