package com.smackmap.smackmapbackend.smack.location.review

import com.smackmap.smackmapbackend.security.SmackerAuthorizationService
import com.smackmap.smackmapbackend.smack.location.SmackLocation
import com.smackmap.smackmapbackend.smack.location.SmackLocationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackLocationReviewService(
    private val authorizationService: SmackerAuthorizationService,
    private val smackLocationService: SmackLocationService,
    private val repository: SmackLocationReviewRepository
) {

    suspend fun addReview(request: SmackLocationReviewRequest): SmackLocation {
        authorizationService.checkAccessFor(request.reviewerId)
        val locationReview = repository
            .findByReviewerIdAndSmackLocationId(request.reviewerId, request.smackLocationId)
        if (locationReview != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT,
                "User '${request.reviewerId}' already reviewed SmackLocation '${request.smackLocationId}'.")
        }
        repository.save(SmackLocationReview(
            smackId = request.smackId,
            reviewerId = request.reviewerId,
            smackLocationId = request.smackLocationId,
            reviewStars = request.reviewStars,
            reviewText = request.reviewText
        ))
        return smackLocationService.updateAverageRating(request.smackLocationId, request.reviewStars)
    }

    suspend fun findAllByLocationIdIn(locationIds: Flow<Long>): Flow<SmackLocationReview> {
        return repository.findAllBySmackLocationIdIn(locationIds.toList())
    }
}