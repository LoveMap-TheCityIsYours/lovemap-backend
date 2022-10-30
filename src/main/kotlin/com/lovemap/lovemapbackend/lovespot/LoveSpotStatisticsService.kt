package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewRequest
import org.springframework.stereotype.Service

@Service
class LoveSpotStatisticsService(
    private val loveSpotService: LoveSpotService,
) {

    suspend fun recordLoveMaking(newLove: Love, countOfLovesAtSpot: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(newLove.loveSpotId)
        setLastLoveAtForNew(loveSpot, newLove)
        setLastActiveAtForNew(loveSpot)
        loveSpot.numberOfLoves = countOfLovesAtSpot
        updatePopularity(loveSpot)
        return loveSpotService.save(loveSpot)
    }

    private fun setLastLoveAtForNew(
        loveSpot: LoveSpot,
        love: Love
    ) {
        loveSpot.lastLoveAt?.let {
            if (love.happenedAt.toInstant().isAfter(it.toInstant())) {
                loveSpot.lastLoveAt = love.happenedAt
            }
        } ?: run {
            loveSpot.lastLoveAt = love.happenedAt
        }
    }

    private fun setLastActiveAtForNew(loveSpot: LoveSpot) {
        loveSpot.lastActiveAt?.let {
            if (loveSpot.lastLoveAt!!.toInstant().isAfter(it.toInstant())) {
                loveSpot.lastActiveAt = loveSpot.lastLoveAt
            }
        } ?: run {
            loveSpot.lastActiveAt = loveSpot.lastLoveAt
        }
    }

    // popularity = 2 * number_of_loves + number_of_comments + occurrence_on_wishlists
    private fun updatePopularity(loveSpot: LoveSpot) {
        loveSpot.popularity = with(loveSpot) {
            2 * numberOfLoves + numberOfComments + occurrenceOnWishlists
        }
    }

    suspend fun deleteLoveMaking(deletedLove: Love, latestLove: Love?, numberOfLoves: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(deletedLove.loveSpotId)
        setLastLoveAt(loveSpot, latestLove)
        setLastActiveAt(loveSpot, latestLove)
        loveSpot.numberOfLoves = numberOfLoves
        updatePopularity(loveSpot)
        return loveSpotService.save(loveSpot)
    }

    private fun setLastLoveAt(loveSpot: LoveSpot, latestLove: Love?) {
        if (latestLove != null) {
            loveSpot.lastLoveAt = latestLove.happenedAt
        } else {
            loveSpot.lastLoveAt = null
        }
    }

    private fun setLastActiveAt(loveSpot: LoveSpot, latestLove: Love?) {
        if (latestLove != null) {
            loveSpot.lastActiveAt = latestLove.happenedAt
        } else {
            loveSpot.lastActiveAt = null
        }
    }

    suspend fun updateLatestLoveMaking(latestLove: Love): LoveSpot {
        val loveSpot = loveSpotService.getById(latestLove.loveSpotId)
        setLastLoveAt(loveSpot, latestLove)
        setLastActiveAt(loveSpot, latestLove)
        return loveSpotService.save(loveSpot)
    }

    suspend fun recalculateLoveSpotReviews(loveSpotId: Long, reviews: List<LoveSpotReview>): LoveSpot {
        val loveSpot = loveSpotService.getById(loveSpotId)
        if (reviews.isNotEmpty()) {
            loveSpot.averageRating = reviews.sumOf { it.reviewStars }.toDouble() / reviews.size
            loveSpot.averageDanger = reviews.sumOf { it.riskLevel }.toDouble() / reviews.size
        } else {
            loveSpot.averageRating = null
            loveSpot.averageDanger = null
        }
        loveSpot.numberOfRatings = reviews.size
        return loveSpotService.save(loveSpot)
    }
}
