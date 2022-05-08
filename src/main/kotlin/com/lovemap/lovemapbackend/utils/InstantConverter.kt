package com.lovemap.lovemapbackend.utils

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class InstantConverter {
    fun fromString(instant: String): Instant {
        try {
            return Instant.ofEpochSecond(
                instant.substringBefore(".").toLong(),
                instant.substringAfter(".").toLong()
            )
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessage(
                ErrorCode.BadRequest,
                instant,
                "Invalid instant format. Valid format is: 'epochSeconds.nanoAdjustment'"
            ).toJson())
        }
    }
}