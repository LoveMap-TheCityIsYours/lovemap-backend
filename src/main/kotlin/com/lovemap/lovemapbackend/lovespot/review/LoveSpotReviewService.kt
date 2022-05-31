package com.lovemap.lovemapbackend.lovespot.review

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.security.AuthorizationService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

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
        val loveSpot = loveSpotService.reviseReviewAverages(spotReview, request)
        loverPointService.updatePointsForReview(spotReview, request, loveSpot)
        spotReview.reviewText = request.reviewText
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
                reviewText = request.reviewText,
                riskLevel = request.riskLevel,
            )
        )
        val loveSpot = loveSpotService.updateReviewAverages(request.loveSpotId, request)
        loverPointService.addPointsForReview(review, loveSpot)
        return loveSpot
    }

    private suspend fun validateReview(request: LoveSpotReviewRequest) {
        checkLocationsMatch(request)
    }

    private suspend fun checkLocationsMatch(
        request: LoveSpotReviewRequest
    ) {
        val love: Love = loveService.getById(request.loveId)
        if (request.loveSpotId != love.loveSpotId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    request.loveSpotId.toString(),
                    "Requested loveSpotId '${request.loveSpotId}' " +
                            "does not match with the love's spotId '${love.loveSpotId}'"
                ).toJson()
            )
        }
    }

    suspend fun deleteReviewsOfSpot(loveSpotId: Long) {
        repository.deleteByLoveSpotId(loveSpotId)
    }

    suspend fun deleteReviewsByLove(love: Love) {
        repository.deleteByReviewerIdAndLoveSpotId(love.loverId, love.loveSpotId)
        love.loverPartnerId?.let {
            repository.deleteByReviewerIdAndLoveSpotId(it, love.loveSpotId)
        }
    }
}