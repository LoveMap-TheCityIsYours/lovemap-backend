package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewResponse
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoverContributionsService(
    private val loveService: LoveService,
    private val spotService: LoveSpotService,
    private val reviewService: LoveSpotReviewService,
) {
    suspend fun list(loverId: Long): LoverContributionsResponse {
        val reviews: Flow<LoveSpotReview> = reviewService.findAllByReviewerId(loverId)
        val loves = loveService.findAllInvolvedLovesFor(loverId).toList()
        val locationIds: List<Long> = loves.map { it.loveSpotId }
        val locations: Flow<LoveSpot> = spotService.findAllByIds(locationIds)
        return LoverContributionsResponse(
            loves = loves,
            loveSpots = locations.map { LoveSpotResponse.of(it) }.toList(),
            loveSpotReviews = reviews.map { LoveSpotReviewResponse.of(it) }.toList()
        )
    }
}