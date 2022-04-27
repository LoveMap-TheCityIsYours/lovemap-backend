package com.smackmap.smackmapbackend.smack.location

import java.time.LocalTime

data class SmackLocationDto(
    val id: Long,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val description: String,
    val averageRating: Double?,
    val numberOfReports: Int,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    var availability: SmackSpotAvailabilityApiStatus,
    var averageDanger: Double?,
    var numberOfRatings: Int,
) {
    companion object {
        fun of(smackLocation: SmackLocation): SmackLocationDto {
            return SmackLocationDto(
                id = smackLocation.id,
                name = smackLocation.name,
                longitude = smackLocation.longitude,
                latitude = smackLocation.latitude,
                averageRating = smackLocation.averageRating,
                averageDanger = smackLocation.averageDanger,
                numberOfRatings = smackLocation.numberOfRatings,
                numberOfReports = smackLocation.numberOfReports,
                description = smackLocation.description,
                customAvailability = smackLocation.readCustomAvailability(),
                availability = SmackSpotAvailabilityApiStatus.of(smackLocation.availability)
            )
        }
    }
}

enum class SmackSpotAvailabilityApiStatus {
    ALL_DAY, NIGHT_ONLY;

    companion object {
        fun of(availability: SmackLocation.Availability): SmackSpotAvailabilityApiStatus {
            return when (availability) {
                SmackLocation.Availability.ALL_DAY -> ALL_DAY
                SmackLocation.Availability.NIGHT_ONLY -> NIGHT_ONLY
            }
        }
    }

    fun toModel(): SmackLocation.Availability {
        return when (this) {
            ALL_DAY -> SmackLocation.Availability.ALL_DAY
            NIGHT_ONLY -> SmackLocation.Availability.NIGHT_ONLY
        }
    }
}

data class CreateSmackLocationRequest(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val description: String,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    var availability: SmackSpotAvailabilityApiStatus
)

data class SmackLocationSearchRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)
