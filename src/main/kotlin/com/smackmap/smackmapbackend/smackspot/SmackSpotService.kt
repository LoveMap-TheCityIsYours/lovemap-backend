package com.smackmap.smackmapbackend.smackspot

import com.smackmap.smackmapbackend.security.AuthorizationService
import com.smackmap.smackmapbackend.utils.ErrorCode
import com.smackmap.smackmapbackend.utils.ErrorMessage
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackSpotService(
    private val authorizationService: AuthorizationService,
    private val repository: SmackSpotRepository
) {
    private val maxLimit = 100

    suspend fun getById(spotId: Long): SmackSpot {
        return repository.findById(spotId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    spotId.toString(),
                    "SmackSpot not found by ID '$spotId'."
                ).toJson()
            )
    }

    suspend fun create(request: CreateSmackSpotRequest): SmackSpot {
        val caller = authorizationService.getCaller()
        val smackSpot = SmackSpot(
            name = request.name,
            longitude = request.longitude,
            latitude = request.latitude,
            addedBy = caller.id,
            description = request.description,
            availability = request.availability.toModel(),
        )
        smackSpot.setCustomAvailability(request.customAvailability)
        // TODO: validate if no locations are within few meters
        return repository.save(smackSpot)
    }

    suspend fun search(request: SmackSpotSearchRequest): Flow<SmackSpot> {
        return repository.search(
            longFrom = request.longFrom,
            longTo = request.longTo,
            latFrom = request.latFrom,
            latTo = request.latTo,
            limit = if (request.limit <= maxLimit) request.limit else maxLimit
        )
    }

    suspend fun updateAverageRating(spotId: Long, rating: Int): SmackSpot {
        val smackSpot = getById(spotId)
        if (smackSpot.averageRating == null) {
            smackSpot.averageRating = rating.toDouble()
            smackSpot.numberOfRatings = 1
        } else {
            var averageWeight = smackSpot.averageRating!! * smackSpot.numberOfRatings
            averageWeight += rating
            smackSpot.numberOfRatings++
            smackSpot.averageRating = averageWeight / smackSpot.numberOfRatings
        }
        return repository.save(smackSpot)
    }

    suspend fun checkExistence(smackSpotId: Long) {
        if (!repository.existsById(smackSpotId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    smackSpotId.toString(),
                    "SmackSpot '$smackSpotId' does not exist."
                ).toJson()
            )
        }
    }

    fun findAllByIds(locationIds: Flow<Long>): Flow<SmackSpot> {
        return repository.findAllById(locationIds)
    }

    fun findAllByIds(locationIds: List<Long>): Flow<SmackSpot> {
        return repository.findAllById(locationIds)
    }
}