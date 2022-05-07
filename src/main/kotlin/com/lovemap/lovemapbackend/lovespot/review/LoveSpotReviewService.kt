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

    suspend fun addReview(request: LoveSpotReviewRequest): LoveSpot {
        authorizationService.checkAccessFor(request.reviewerId)
        validateReview(request)
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
        return loveSpotService.updateAverageRating(request.loveSpotId, request.reviewStars)
    }

    private suspend fun validateReview(request: LoveSpotReviewRequest) {
        val love: Love = getLoveAndCheckIsPartOfIt(request)
        checkNotReviewedYet(request)
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

    private suspend fun checkNotReviewedYet(request: LoveSpotReviewRequest) {
        val spotReview = repository
            .findByReviewerIdAndLoveSpotId(request.reviewerId, request.loveSpotId)
        if (spotReview != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    ErrorCode.Conflict,
                    request.loveSpotId.toString(),
                    "User '${request.reviewerId}' already reviewed LoveSpot '${request.loveSpotId}'."
                ).toJson()
            )
        }
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