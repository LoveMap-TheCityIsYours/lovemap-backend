package com.smackmap.smackmapbackend.smack.location.review

import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.smack.Smack
import com.smackmap.smackmapbackend.smack.SmackService
import com.smackmap.smackmapbackend.smack.location.SmackLocation
import com.smackmap.smackmapbackend.smack.location.SmackLocationService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackLocationReviewService(
    private val authorizationService: AuthorizationService,
    private val smackService: SmackService,
    private val smackLocationService: SmackLocationService,
    private val repository: SmackLocationReviewRepository
) {
    fun findAllByLocationIdIn(locationIds: List<Long>): Flow<SmackLocationReview> {
        return repository.findAllBySmackLocationIdIn(locationIds)
    }

    fun findAllByReviewerId(reviewerId: Long): Flow<SmackLocationReview> {
        return repository.findAllByReviewerId(reviewerId)
    }

    suspend fun addReview(request: SmackLocationReviewRequest): SmackLocation {
        authorizationService.checkAccessFor(request.reviewerId)
        validateReview(request)
        repository.save(
            SmackLocationReview(
                smackId = request.smackId,
                reviewerId = request.reviewerId,
                smackLocationId = request.smackLocationId,
                reviewStars = request.reviewStars,
                reviewText = request.reviewText
            )
        )
        return smackLocationService.updateAverageRating(request.smackLocationId, request.reviewStars)
    }

    private suspend fun validateReview(request: SmackLocationReviewRequest) {
        val smack: Smack = getSmackAndCheckIsPartOfIt(request)
        checkNotReviewedYet(request)
        checkLocationsMatch(request, smack)
    }

    private suspend fun getSmackAndCheckIsPartOfIt(request: SmackLocationReviewRequest): Smack {
        val smack: Smack = smackService.getById(request.smackId)
        if (!smackService.isSmackerOrPartnerInSmack(request.reviewerId, smack)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot review this smack.")
        }
        return smack
    }

    private suspend fun checkNotReviewedYet(request: SmackLocationReviewRequest) {
        val locationReview = repository
            .findByReviewerIdAndSmackLocationId(request.reviewerId, request.smackLocationId)
        if (locationReview != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "User '${request.reviewerId}' already reviewed SmackLocation '${request.smackLocationId}'."
            )
        }
    }

    private fun checkLocationsMatch(
        request: SmackLocationReviewRequest,
        smack: Smack
    ) {
        if (request.smackLocationId != smack.smackLocationId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Requested smackLocationId '${request.smackLocationId}' " +
                        "does not match with the smack's locationId '${smack.smackLocationId}'"
            )
        }
    }
}