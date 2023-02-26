package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import kotlin.math.max

@Service
class LoveSpotStatisticsService(
    private val loveSpotService: LoveSpotService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun recordLoveMaking(newLove: Love, countOfLovesAtSpot: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(newLove.loveSpotId)
        setLastLoveAtForNewLove(loveSpot, newLove)
        setLastActiveAtForNewLove(loveSpot)
        loveSpot.numberOfLoves = countOfLovesAtSpot
        updatePopularity(loveSpot)
        return loveSpotService.save(loveSpot)
    }

    private fun setLastLoveAtForNewLove(
        loveSpot: LoveSpot,
        love: Love
    ) {
        loveSpot.lastLoveAt?.let {
            if (love.happenedAt.toInstant().isAfter(it.toInstant())) {
                logger.info { "Updating existing lastLoveAt for LoveSpot '${loveSpot.id}'" }
                loveSpot.lastLoveAt = love.happenedAt
            } else {
                logger.info { "Keeping existing lastLoveAt for LoveSpot '${loveSpot.id}'" }
            }
        } ?: run {
            logger.info { "Setting new lastLoveAt for LoveSpot '${loveSpot.id}'" }
            loveSpot.lastLoveAt = love.happenedAt
        }
    }

    private fun setLastActiveAtForNewLove(loveSpot: LoveSpot) {
        loveSpot.lastActiveAt?.let {
            if (loveSpot.lastLoveAt!!.toInstant().isAfter(it.toInstant())) {
                logger.info { "Updating existing lastActiveAt for LoveSpot '${loveSpot.id}'" }
                loveSpot.lastActiveAt = loveSpot.lastLoveAt
            } else {
                logger.info { "Keeping existing lastActiveAt for LoveSpot '${loveSpot.id}'" }
            }
        } ?: run {
            logger.info { "Setting new lastActiveAt for LoveSpot '${loveSpot.id}'" }
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

    suspend fun updatePhotoStats(loveSpot: LoveSpot, photoCount: Int): LoveSpot {
        loveSpot.numberOfPhotos += photoCount
        loveSpot.lastPhotoAddedAt = Timestamp.from(Instant.now())
        return loveSpotService.save(loveSpot)
    }

    suspend fun decrementNumberOfPhotos(loveSpotId: Long) {
        val loveSpot = loveSpotService.getById(loveSpotId)
        loveSpot.numberOfPhotos = max(loveSpot.numberOfPhotos - 1, 0)
        loveSpotService.save(loveSpot)
    }

    suspend fun changeWishlistOccurrence(loveSpotId: Long, plusOccurrence: Int) {
        val loveSpot = loveSpotService.getById(loveSpotId)
        loveSpot.occurrenceOnWishlists = max(loveSpot.occurrenceOnWishlists + plusOccurrence, 0)
        updatePopularity(loveSpot)
        loveSpotService.save(loveSpot)
    }

    suspend fun updateNumberOfReports(loveSpotId: Long): LoveSpot {
        val loveSpot = loveSpotService.getById(loveSpotId)
        loveSpot.numberOfReports += 1
        return loveSpotService.save(loveSpot)
    }
}
