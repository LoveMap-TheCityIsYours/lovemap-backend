package com.lovemap.lovemapbackend.lovespot.review

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LoveSpotReviewService(
    private val authorizationService: AuthorizationService,
    private val loveService: LoveService,
    private val loveSpotService: LoveSpotService,
    private val loverPointService: LoverPointService,
    private val repository: LoveSpotReviewRepository
) {

    fun findAllByLoveSpotIdIn(loveSpotIds: List<Long>): Flow<LoveSpotReview> {
        return repository.findAllByLoveSpotIdIn(loveSpotIds)
    }

    fun findAllByReviewerId(reviewerId: Long): Flow<LoveSpotReview> {
        return repository.findAllByReviewerId(reviewerId)
    }

    suspend fun addOrUpdateReview(request: LoveSpotReviewRequest): LoveSpot {
        authorizationService.checkAccessFor(request.reviewerId)
        validateReview(request)
        val spotReview = repository
            .findByReviewerIdAndLoveSpotId(request.reviewerId, request.loveSpotId)
        if (spotReview != null) {
            return updateReview(spotReview, request)
        }
        return addReview(request)
    }

    private suspend fun updateReview(
        spotReview: LoveSpotReview,
        request: LoveSpotReviewRequest
    ): LoveSpot {
        val loveSpot = updateAveragesForChangedReview(spotReview, request)
        loverPointService.updatePointsForReview(spotReview, request, loveSpot)
        spotReview.reviewText = request.reviewText.trim()
        spotReview.reviewStars = request.reviewStars
        spotReview.riskLevel = request.riskLevel
        repository.save(spotReview)
        return loveSpot
    }

    private suspend fun addReview(request: LoveSpotReviewRequest): LoveSpot {
        val review = repository.save(
            LoveSpotReview(
                loveId = request.loveId,
                reviewerId = request.reviewerId,
                loveSpotId = request.loveSpotId,
                reviewStars = request.reviewStars,
                reviewText = request.reviewText.trim(),
                riskLevel = request.riskLevel,
            )
        )
        val loveSpot = updateAveragesForNewReview(request.loveSpotId, request)
        loverPointService.addPointsForReview(review, loveSpot)
        return loveSpot
    }

    suspend fun updateAveragesForNewReview(spotId: Long, request: LoveSpotReviewRequest): LoveSpot {
        val loveSpot = loveSpotService.getById(spotId)
        if (loveSpot.averageRating == null) {
            loveSpot.averageRating = request.reviewStars.toDouble()
            loveSpot.averageDanger = request.riskLevel.toDouble()
            loveSpot.numberOfRatings = 1
        } else {
            var averageRatingWeight = loveSpot.averageRating!! * loveSpot.numberOfRatings
            var averageDangerWeight = loveSpot.averageDanger!! * loveSpot.numberOfRatings
            loveSpot.numberOfRatings++

            averageRatingWeight += request.reviewStars
            loveSpot.averageRating = averageRatingWeight / loveSpot.numberOfRatings

            averageDangerWeight += request.riskLevel
            loveSpot.averageDanger = averageDangerWeight / loveSpot.numberOfRatings
        }
        return loveSpotService.save(loveSpot)
    }

    suspend fun updateAveragesForChangedReview(previousReview: LoveSpotReview, request: LoveSpotReviewRequest): LoveSpot {
        val loveSpot = loveSpotService.getById(previousReview.loveSpotId)
        var averageRatingWeight = loveSpot.averageRating!! * loveSpot.numberOfRatings
        var averageDangerWeight = loveSpot.averageDanger!! * loveSpot.numberOfRatings
        averageRatingWeight = averageRatingWeight - previousReview.reviewStars + request.reviewStars
        averageDangerWeight = averageDangerWeight - previousReview.riskLevel + request.riskLevel
        loveSpot.averageRating = averageRatingWeight / loveSpot.numberOfRatings
        loveSpot.averageDanger = averageDangerWeight / loveSpot.numberOfRatings
        return loveSpotService.save(loveSpot)
    }

    private suspend fun validateReview(request: LoveSpotReviewRequest) {
        checkLocationsMatch(request)
    }

    private suspend fun checkLocationsMatch(
        request: LoveSpotReviewRequest
    ) {
        val love: Love = loveService.getById(request.loveId)
        if (request.loveSpotId != love.loveSpotId) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    request.loveSpotId.toString(),
                    "Requested loveSpotId '${request.loveSpotId}' " +
                            "does not match with the love's spotId '${love.loveSpotId}'"
                )
            )
        }
    }

    suspend fun deleteReviewsOfSpot(loveSpotId: Long) {
        repository.deleteByLoveSpotId(loveSpotId)
    }

    suspend fun deleteReviewsByLove(love: Love) {
        val loveSpot = loveSpotService.getById(love.loveSpotId)
        val review = repository.findByReviewerIdAndLoveId(love.loverId, love.id)
        if (review != null) {
            repository.delete(review)
            loveSpot.numberOfRatings--
        }
        love.loverPartnerId?.let {
            val partnerReview = repository.findByReviewerIdAndLoveId(it, love.id)
            if (partnerReview != null) {
                repository.delete(partnerReview)
                loveSpot.numberOfRatings--
            }
        }
        if (loveSpot.numberOfRatings == 0) {
            loveSpot.averageRating = null
            loveSpot.averageDanger = null
        }
        loveSpotService.save(loveSpot)
    }
}