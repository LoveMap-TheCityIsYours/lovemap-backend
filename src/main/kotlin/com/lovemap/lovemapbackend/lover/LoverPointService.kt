package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lover.points.LoverPoints
import com.lovemap.lovemapbackend.lover.ranks.LoverRanks
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.report.LoveSpotReport
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewRequest
import org.springframework.stereotype.Service

@Service
class LoverPointService(
    private val loverService: LoverService,
    private val points: LoverPoints,
    private val loverRanks: LoverRanks
) {

    suspend fun addPointsForReview(review: LoveSpotReview, loveSpot: LoveSpot): Lover {
        val lover = addPointsForReviewSubmitted(review)
        addPointsForReviewReceived(review, loveSpot)
        return lover
    }

    private suspend fun addPointsForReviewSubmitted(review: LoveSpotReview): Lover {
        val lover = loverService.unAuthorizedGetById(review.reviewerId)
        if (isReviewMeaningful(review.reviewText)) {
            lover.points += points.reviewSubmitted
            lover.reviewsSubmitted += 1
        }
        return updateRank(lover)
    }

    private suspend fun addPointsForReviewReceived(review: LoveSpotReview, loveSpot: LoveSpot) {
        if (review.reviewStars >= 4) {
            val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
            spotAdder.points += if (review.reviewStars == 4) {
                points.reviewReceived4Stars
            } else {
                points.reviewReceived5Stars
            }
            updateRank(spotAdder)
        }
    }

    suspend fun updatePointsForReview(prevReview: LoveSpotReview, newRequest: LoveSpotReviewRequest, loveSpot: LoveSpot): Lover {
        val lover = loverService.unAuthorizedGetById(prevReview.reviewerId)
        updatePointsForReviewSubmitted(prevReview, newRequest, lover)
        updatePointsForReviewReceived(loveSpot, prevReview, newRequest)
        return lover
    }

    private suspend fun updatePointsForReviewSubmitted(
        prevReview: LoveSpotReview,
        newRequest: LoveSpotReviewRequest,
        lover: Lover
    ) {
        if (isReviewMeaningful(prevReview.reviewText)) {
            if (isReviewMeaningful(newRequest.reviewText)) {
                // do nothing
            } else {
                // subtract points from lover
                lover.points -= points.reviewSubmitted
                lover.reviewsSubmitted -= 1
            }
        } else {
            if (isReviewMeaningful(newRequest.reviewText)) {
                // add points to lover
                lover.points += points.reviewSubmitted
                lover.reviewsSubmitted += 1
            } else {
                // do nothing
            }
        }
        updateRank(lover)
    }

    private suspend fun updatePointsForReviewReceived(
        loveSpot: LoveSpot,
        prevReview: LoveSpotReview,
        newRequest: LoveSpotReviewRequest
    ) {
        val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
        if (prevReview.reviewStars == 4) {
            if (newRequest.reviewStars == 4) {
                // do nothing
            } else if (newRequest.reviewStars >= 5) {
                spotAdder.points = spotAdder.points - points.reviewReceived4Stars + points.reviewReceived5Stars
            }
        } else if (prevReview.reviewStars >= 5) {
            if (newRequest.reviewStars == 4) {
                spotAdder.points = spotAdder.points - points.reviewReceived5Stars + points.reviewReceived4Stars
            } else if (newRequest.reviewStars >= 5) {
                // do nothing
            }
        }
        updateRank(spotAdder)
    }

    suspend fun addPointsForReport(report: LoveSpotReport, loveSpot: LoveSpot): Lover {
        val lover = addPointsForReportSubmitted(report)
        addPointsForReportReceived(loveSpot)
        return lover
    }

    private suspend fun addPointsForReportSubmitted(report: LoveSpotReport): Lover {
        val lover = loverService.unAuthorizedGetById(report.loverId)
        lover.points += points.reportSubmitted
        lover.reportsSubmitted += 1
        return updateRank(lover)
    }

    private suspend fun addPointsForReportReceived(loveSpot: LoveSpot) {
        val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
        spotAdder.points += points.reportReceived
        spotAdder.reportsReceived += 1
        updateRank(spotAdder)
    }

    suspend fun addPointsForLovemaking(love: Love): Lover {
        val lover = loverService.unAuthorizedGetById(love.loverId)
        lover.points += points.loveMade
        lover.numberOfLoves += 1
        updateRank(lover)
        love.loverPartnerId?.let {
            val partner = loverService.unAuthorizedGetById(it)
            partner.points += points.loveMade
            partner.numberOfLoves += 1
            updateRank(partner)
        }
        return lover
    }

    suspend fun addPointsForSpotAdded(loveSpot: LoveSpot): Lover {
        val lover = loverService.unAuthorizedGetById(loveSpot.addedBy)
        lover.points += points.loveSpotAdded
        lover.loveSpotsAdded += 1
        return updateRank(lover)
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

    private fun isReviewMeaningful(reviewText: String) =
        reviewText.length > 10
}