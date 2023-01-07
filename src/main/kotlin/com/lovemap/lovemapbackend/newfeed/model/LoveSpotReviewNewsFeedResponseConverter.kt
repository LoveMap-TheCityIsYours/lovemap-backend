package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoveSpotReviewNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<LoveSpotReviewNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_REVIEW
    }

    override fun convert(dto: NewsFeedItemDto): LoveSpotReviewNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is LoveSpotReviewNewsFeedData) {
            LoveSpotReviewNewsFeedResponse(
                id = newsFeedData.id,
                loveSpotId = newsFeedData.loveSpotId,
                reviewerId = newsFeedData.reviewerId,
                submittedAt = newsFeedData.submittedAt,
                reviewText = newsFeedData.reviewText,
                reviewStars = newsFeedData.reviewStars,
                riskLevel = newsFeedData.riskLevel
            )
        } else {
            null
        }
    }

}
