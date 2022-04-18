package com.smackmap.smackmapbackend.smack.location

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SmackLocationService(
    private val smackLocationRepository: SmackLocationRepository
) {
    suspend fun create(request: CreateSmackLocationRequest): SmackLocation {
        val smackLocation = SmackLocation(
            name = request.name,
            longitude = request.longitude,
            latitude = request.latitude,
        )
        // TODO: validate if no locations are within few meters
        return smackLocationRepository.save(smackLocation)
    }

}