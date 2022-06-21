package com.lovemap.lovemapbackend.utils

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

object InstantConverterUtils {

    fun Instant.toApiString(): String {
        return "$epochSecond.$nano"
    }

    fun fromString(instant: String): Instant {
        try {
            return Instant.ofEpochSecond(
                instant.substringBefore(".").toLong(),
                instant.substringAfter(".").toLong()
            )
        } catch (e: Exception) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST, ErrorMessage(
                    ErrorCode.BadRequest,
                    instant,
                    "Invalid instant format. Valid format is: 'epochSeconds.nanoAdjustment'"
                )
            )
        }
    }
}