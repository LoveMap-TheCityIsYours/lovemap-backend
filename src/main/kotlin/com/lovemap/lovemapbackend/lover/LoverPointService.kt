package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lover.points.LoverPoints
import com.lovemap.lovemapbackend.lover.ranks.LoverRanks
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.report.LoveSpotReport
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import org.springframework.stereotype.Service

@Service
class LoverPointService(
    private val loverService: LoverService,
    private val loverPoints: LoverPoints,
    private val loverRanks: LoverRanks
) {

    suspend fun addPointsForReview(review: LoveSpotReview, loveSpot: LoveSpot): Lover {
        val lover = addPointsForReviewSubmitted(review)
        addPointsForReviewReceived(review, loveSpot)
        return lover
    }

    suspend fun addPointsForReport(report: LoveSpotReport, loveSpot: LoveSpot): Lover {
        val lover = addPointsForReportSubmitted(report)
        addPointsForReportReceived(loveSpot)
        return lover
    }

    suspend fun addPointsForLovemaking(love: Love): Lover {
        val lover = loverService.unAuthorizedGetById(love.loverId)
        lover.points += loverPoints.loveMade
        lover.numberOfLoves += 1
        updateRank(lover)
        love.loverPartnerId?.let {
            val partner = loverService.unAuthorizedGetById(it)
            partner.points += loverPoints.loveMade
            partner.numberOfLoves += 1
            updateRank(partner)
        }
        return lover
    }

    suspend fun addPointsForSpotAdded(loveSpot: LoveSpot): Lover {
        val lover = loverService.unAuthorizedGetById(loveSpot.addedBy)
        lover.points += loverPoints.loveSpotAdded
        lover.loveSpotsAdded += 1
        return updateRank(lover)
    }

    private suspend fun addPointsForReviewSubmitted(review: LoveSpotReview): Lover {
        val lover = loverService.unAuthorizedGetById(review.reviewerId)
        if (review.reviewText.length > 10) {
            lover.points += loverPoints.reviewSubmitted
            lover.reviewsSubmitted += 1
        }
        return updateRank(lover)
    }

    private suspend fun addPointsForReviewReceived(review: LoveSpotReview, loveSpot: LoveSpot) {
        if (review.reviewStars >= 4) {
            val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
            spotAdder.points += if (review.reviewStars == 4) {
                loverPoints.reviewReceived4Stars
            } else {
                loverPoints.reviewReceived5Stars
            }
            updateRank(spotAdder)
        }
    }

    private suspend fun addPointsForReportSubmitted(report: LoveSpotReport): Lover {
        val lover = loverService.unAuthorizedGetById(report.loverId)
        lover.points += loverPoints.reportSubmitted
        lover.reportsSubmitted += 1
        return updateRank(lover)
    }

    private suspend fun addPointsForReportReceived(loveSpot: LoveSpot) {
        val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
        spotAdder.points += loverPoints.reportReceived
        spotAdder.reportsReceived += 1
        updateRank(spotAdder)
    }

    private suspend fun updateRank(lover: Lover): Lover {
        lover.rank = calculateRank(lover.points)
        return loverService.save(lover)
    }

    private fun calculateRank(points: Int): Int {
        val rankList = loverRanks.rankList
        var rankLevel = 1
        for ((index, rank) in rankList.withIndex()) {
            rankLevel = if (rank.pointsNeeded > points) {
                rankLevel = index
                break
            } else {
                1
            }
        }
        return rankLevel
    }
}