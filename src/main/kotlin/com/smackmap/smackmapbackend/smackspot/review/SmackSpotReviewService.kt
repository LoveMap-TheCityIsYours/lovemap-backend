package com.smackmap.smackmapbackend.smackspot.review

import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.smack.Smack
import com.smackmap.smackmapbackend.smack.SmackService
import com.smackmap.smackmapbackend.smackspot.SmackSpot
import com.smackmap.smackmapbackend.smackspot.SmackSpotService
import com.smackmap.smackmapbackend.utils.ErrorCode
import com.smackmap.smackmapbackend.utils.ErrorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackSpotReviewService(
    private val authorizationService: AuthorizationService,
    private val smackService: SmackService,
    private val smackSpotService: SmackSpotService,
    private val repository: SmackSpotReviewRepository
) {
    fun findAllByLocationIdIn(locationIds: List<Long>): Flow<SmackSpotReview> {
        return repository.findAllBySmackSpotIdIn(locationIds)
    }

    fun findAllByReviewerId(reviewerId: Long): Flow<SmackSpotReview> {
        return repository.findAllByReviewerId(reviewerId)
    }

    suspend fun addReview(request: SmackSpotReviewRequest): SmackSpot {
        authorizationService.checkAccessFor(request.reviewerId)
        validateReview(request)
        repository.save(
            SmackSpotReview(
                smackId = request.smackId,
                reviewerId = request.reviewerId,
                smackSpotId = request.smackSpotId,
                reviewStars = request.reviewStars,
                reviewText = request.reviewText
            )
        )
        return smackSpotService.updateAverageRating(request.smackSpotId, request.reviewStars)
    }

    private suspend fun validateReview(request: SmackSpotReviewRequest) {
        val smack: Smack = getSmackAndCheckIsPartOfIt(request)
        checkNotReviewedYet(request)
        checkLocationsMatch(request, smack)
    }

    private suspend fun getSmackAndCheckIsPartOfIt(request: SmackSpotReviewRequest): Smack {
        val smack: Smack = smackService.getById(request.smackId)
        if (!smackService.isSmackerOrPartnerInSmack(request.reviewerId, smack)) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ErrorMessage(
                    ErrorCode.Forbidden,
                    request.smackId.toString(),
                    "You cannot review this smack."
                ).toJson()
            )
        }
        return smack
    }

    private suspend fun checkNotReviewedYet(request: SmackSpotReviewRequest) {
        val spotReview = repository
            .findByReviewerIdAndSmackSpotId(request.reviewerId, request.smackSpotId)
        if (spotReview != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                ErrorMessage(
                    ErrorCode.Conflict,
                    request.smackSpotId.toString(),
                    "User '${request.reviewerId}' already reviewed SmackSpot '${request.smackSpotId}'."
                ).toJson()
            )
        }
    }

    private fun checkLocationsMatch(
        request: SmackSpotReviewRequest,
        smack: Smack
    ) {
        if (request.smackSpotId != smack.smackSpotId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    request.smackSpotId.toString(),
                    "Requested smackSpotId '${request.smackSpotId}' " +
                            "does not match with the smack's spotId '${smack.smackSpotId}'"
                ).toJson()

            )
        }
    }
}