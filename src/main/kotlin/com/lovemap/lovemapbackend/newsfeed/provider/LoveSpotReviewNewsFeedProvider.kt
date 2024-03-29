package com.lovemap.lovemapbackend.newsfeed.provider

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.LoveSpotReviewNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoveSpotReviewNewsFeedProvider(
    private val reviewService: LoveSpotReviewService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val cachedLoverService: CachedLoverService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for LoveSpot Reviews from $generateFrom" }
        val reviews = reviewService.getPhotosFrom(generateFrom)
        return reviews.map {
            NewsFeedItemDto(
                type = NewsFeedItemDto.Type.LOVE_SPOT_REVIEW,
                generatedAt = generationTime,
                referenceId = it.id,
                country = cachedLoveSpotService.getCountryByLoveSpotId(it.loveSpotId),
                publicLover = cachedLoverService.getIfProfileIsPublic(it.reviewerId),
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
