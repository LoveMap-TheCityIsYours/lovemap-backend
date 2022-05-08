package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewRequest
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
class LoveSpotService(
    private val authorizationService: AuthorizationService,
    private val repository: LoveSpotRepository
) {
    private val maxLimit = 100

    suspend fun getById(spotId: Long): LoveSpot {
        return repository.findById(spotId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    spotId.toString(),
                    "LoveSpot not found by ID '$spotId'."
                ).toJson()
            )
    }

    suspend fun create(request: CreateLoveSpotRequest): LoveSpot {
        val caller = authorizationService.getCaller()
        val loveSpot = LoveSpot(
            name = request.name,
            longitude = request.longitude,
            latitude = request.latitude,
            addedBy = caller.id,
            description = request.description,
            availability = request.availability.toModel(),
        )
        loveSpot.setCustomAvailability(request.customAvailability)
        // TODO: validate if no locations are within few meters
        return repository.save(loveSpot)
    }

    suspend fun search(request: LoveSpotSearchRequest): Flow<LoveSpot> {
        return repository.search(
            longFrom = request.longFrom,
            longTo = request.longTo,
            latFrom = request.latFrom,
            latTo = request.latTo,
            limit = if (request.limit <= maxLimit) request.limit else maxLimit
        )
    }

    suspend fun updateReviewAverages(spotId: Long, request: LoveSpotReviewRequest): LoveSpot {
        val loveSpot = getById(spotId)
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
        return repository.save(loveSpot)
    }

    suspend fun reviseReviewAverages(previousReview: LoveSpotReview, request: LoveSpotReviewRequest): LoveSpot {
        val loveSpot = getById(previousReview.loveSpotId)
        var averageRatingWeight = loveSpot.averageRating!! * loveSpot.numberOfRatings
        var averageDangerWeight = loveSpot.averageDanger!! * loveSpot.numberOfRatings
        averageRatingWeight = averageRatingWeight - previousReview.reviewStars + request.reviewStars
        averageDangerWeight = averageDangerWeight - previousReview.riskLevel + request.riskLevel
        loveSpot.averageRating = averageRatingWeight / loveSpot.numberOfRatings
        loveSpot.averageDanger = averageDangerWeight / loveSpot.numberOfRatings
        return repository.save(loveSpot)
    }

    suspend fun checkExistence(loveSpotId: Long) {
        if (!repository.existsById(loveSpotId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    loveSpotId.toString(),
                    "LoveSpot '$loveSpotId' does not exist."
                ).toJson()
            )
        }
    }

    fun findAllByIds(locationIds: Flow<Long>): Flow<LoveSpot> {
        return repository.findAllById(locationIds)
    }

    fun findAllByIds(locationIds: List<Long>): Flow<LoveSpot> {
        return repository.findAllById(locationIds)
    }
}