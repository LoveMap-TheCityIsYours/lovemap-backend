package com.smackmap.smackmapbackend.smack.location

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
class SmackLocationService(
    private val authorizationService: AuthorizationService,
    private val repository: SmackLocationRepository
) {
    private val maxLimit = 100

    suspend fun getById(locationId: Long): SmackLocation {
        return repository.findById(locationId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    locationId.toString(),
                    "SmackLocation not found by ID '$locationId'."
                ).toJson()
            )
    }

    suspend fun create(request: CreateSmackLocationRequest): SmackLocation {
        val caller = authorizationService.getCaller()
        val smackLocation = SmackLocation(
            name = request.name,
            longitude = request.longitude,
            latitude = request.latitude,
            addedBy = caller.id,
            description = request.description,
            availability = request.availability.toModel(),
        )
        smackLocation.setCustomAvailability(request.customAvailability)
        // TODO: validate if no locations are within few meters
        return repository.save(smackLocation)
    }

    suspend fun search(request: SmackLocationSearchRequest): Flow<SmackLocation> {
        return repository.search(
            longFrom = request.longFrom,
            longTo = request.longTo,
            latFrom = request.latFrom,
            latTo = request.latTo,
            limit = if (request.limit <= maxLimit) request.limit else maxLimit
        )
    }

    suspend fun updateAverageRating(locationId: Long, rating: Int): SmackLocation {
        val smackLocation = getById(locationId)
        if (smackLocation.averageRating == null) {
            smackLocation.averageRating = rating.toDouble()
            smackLocation.numberOfRatings = 1
        } else {
            var averageWeight = smackLocation.averageRating!! * smackLocation.numberOfRatings
            averageWeight += rating
            smackLocation.numberOfRatings++
            smackLocation.averageRating = averageWeight / smackLocation.numberOfRatings
        }
        return repository.save(smackLocation)
    }

    suspend fun checkExistence(smackLocationId: Long) {
        if (!repository.existsById(smackLocationId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    smackLocationId.toString(),
                    "SmackLocation '$smackLocationId' does not exist."
                ).toJson()
            )
        }
    }

    fun findAllByIds(locationIds: Flow<Long>): Flow<SmackLocation> {
        return repository.findAllById(locationIds)
    }

    fun findAllByIds(locationIds: List<Long>): Flow<SmackLocation> {
        return repository.findAllById(locationIds)
    }
}