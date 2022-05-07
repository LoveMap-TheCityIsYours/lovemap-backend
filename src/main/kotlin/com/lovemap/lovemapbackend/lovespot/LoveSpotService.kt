package com.lovemap.lovemapbackend.lovespot

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

    suspend fun updateAverageRating(spotId: Long, rating: Int): LoveSpot {
        val loveSpot = getById(spotId)
        if (loveSpot.averageRating == null) {
            loveSpot.averageRating = rating.toDouble()
            loveSpot.numberOfRatings = 1
        } else {
            var averageWeight = loveSpot.averageRating!! * loveSpot.numberOfRatings
            averageWeight += rating
            loveSpot.numberOfRatings++
            loveSpot.averageRating = averageWeight / loveSpot.numberOfRatings
        }
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