package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoveSpotReviewNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoveSpotReviewNewsFeedProvider(
    private val reviewService: LoveSpotReviewService
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for LoveSpot Reviews from $generateFrom" }
        val reviews = reviewService.getPhotosFrom(generateFrom)
        return reviews.map {
            NewsFeedItemDto(
                type = NewsFeedItem.Type.LOVE_SPOT_REVIEW,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = reviewToNewsFeedData(it)
            )
        }
    }

    private suspend fun reviewToNewsFeedData(review: LoveSpotReview): LoveSpotReviewNewsFeedData {
        return LoveSpotReviewNewsFeedData(
            id = review.id,
            loveSpotId = review.loveSpotId,
            reviewerId = review.reviewerId,
            submittedAt = review.submittedAt.toInstant(),
            reviewText = review.reviewText,
            reviewStars = review.reviewStars,
            riskLevel = review.riskLevel
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_REVIEW
    }
}
