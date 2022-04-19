package com.smackmap.smackmapbackend.smack.location

import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class SmackLocationService(
    private val smackLocationRepository: SmackLocationRepository
) {
    private val maxLimit = 100

    suspend fun getById(locationId: Long): SmackLocation {
        return smackLocationRepository.findById(locationId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "SmackLocation not found by ID '$locationId'.")
    }

    suspend fun create(request: CreateSmackLocationRequest): SmackLocation {
        val smackLocation = SmackLocation(
            name = request.name,
            longitude = request.longitude,
            latitude = request.latitude,
        )
        // TODO: validate if no locations are within few meters
        return smackLocationRepository.save(smackLocation)
    }

    suspend fun search(request: SmackLocationSearchRequest): Flow<SmackLocation> {
        return smackLocationRepository.search(
            longFrom = request.longFrom,
            longTo = request.longTo,
            latFrom = request.latFrom,
            latTo = request.latTo,
            limit = if (request.limit <= maxLimit) request.limit else maxLimit
        )
    }

    suspend fun updateRating(locationId: Long, rating: Int): SmackLocation {
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
        return smackLocationRepository.save(smackLocation)
    }
}