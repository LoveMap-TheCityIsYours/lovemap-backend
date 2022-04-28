package com.smackmap.smackmapbackend.smackspot

import java.time.LocalTime

data class SmackSpotDto(
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
        fun of(smackSpot: SmackSpot): SmackSpotDto {
            return SmackSpotDto(
                id = smackSpot.id,
                name = smackSpot.name,
                longitude = smackSpot.longitude,
                latitude = smackSpot.latitude,
                averageRating = smackSpot.averageRating,
                averageDanger = smackSpot.averageDanger,
                numberOfRatings = smackSpot.numberOfRatings,
                numberOfReports = smackSpot.numberOfReports,
                description = smackSpot.description,
                customAvailability = smackSpot.readCustomAvailability(),
                availability = SmackSpotAvailabilityApiStatus.of(smackSpot.availability)
            )
        }
    }
}

enum class SmackSpotAvailabilityApiStatus {
    ALL_DAY, NIGHT_ONLY;

    companion object {
        fun of(availability: SmackSpot.Availability): SmackSpotAvailabilityApiStatus {
            return when (availability) {
                SmackSpot.Availability.ALL_DAY -> ALL_DAY
                SmackSpot.Availability.NIGHT_ONLY -> NIGHT_ONLY
            }
        }
    }

    fun toModel(): SmackSpot.Availability {
        return when (this) {
            ALL_DAY -> SmackSpot.Availability.ALL_DAY
            NIGHT_ONLY -> SmackSpot.Availability.NIGHT_ONLY
        }
    }
}

data class CreateSmackSpotRequest(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val description: String,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    var availability: SmackSpotAvailabilityApiStatus
)

data class SmackSpotSearchRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)
