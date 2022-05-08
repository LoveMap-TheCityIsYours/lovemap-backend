package com.lovemap.lovemapbackend.lovespot.review

import com.lovemap.lovemapbackend.security.AuthorizationService
import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
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
        spotReview.reviewText = request.reviewText
        val loveSpot = loveSpotService.reviseReviewAverages(spotReview, request)
        spotReview.reviewStars = request.reviewStars
        spotReview.riskLevel = request.riskLevel
        repository.save(spotReview)
        return loveSpot
    }

    private suspend fun addReview(request: LoveSpotReviewRequest): LoveSpot {
        repository.save(
            LoveSpotReview(
                loveId = request.loveId,
                reviewerId = request.reviewerId,
                loveSpotId = request.loveSpotId,
                reviewStars = request.reviewStars,
                reviewText = request.reviewText,
                riskLevel = request.riskLevel,
            )
        )
        return loveSpotService.updateReviewAverages(request.loveSpotId, request)
    }

    private suspend fun validateReview(request: LoveSpotReviewRequest) {
        val love: Love = getLoveAndCheckIsPartOfIt(request)
        checkLocationsMatch(request, love)
    }

    private suspend fun getLoveAndCheckIsPartOfIt(request: LoveSpotReviewRequest): Love {
        val love: Love = loveService.getById(request.loveId)
        if (!loveService.isLoverOrPartnerInLove(request.reviewerId, love)) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    ErrorCode.Forbidden,
                    request.loveId.toString(),
                    "You cannot review this love."
                ).toJson()
            )
        }
        return love
    }

    private fun checkLocationsMatch(
        request: LoveSpotReviewRequest,
        love: Love
    ) {
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
}