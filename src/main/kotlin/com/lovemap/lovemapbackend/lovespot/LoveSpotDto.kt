package com.lovemap.lovemapbackend.lovespot

import java.time.LocalTime

data class LoveSpotDto(
    val id: Long,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val description: String,
    val averageRating: Double?,
    val numberOfReports: Int,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    var availability: LoveSpotAvailabilityApiStatus,
    var averageDanger: Double?,
    var numberOfRatings: Int,
) {
    companion object {
        fun of(loveSpot: LoveSpot): LoveSpotDto {
            return LoveSpotDto(
                id = loveSpot.id,
                name = loveSpot.name,
                longitude = loveSpot.longitude,
                latitude = loveSpot.latitude,
                averageRating = loveSpot.averageRating,
                averageDanger = loveSpot.averageDanger,
                numberOfRatings = loveSpot.numberOfRatings,
                numberOfReports = loveSpot.numberOfReports,
                description = loveSpot.description,
                customAvailability = loveSpot.readCustomAvailability(),
                availability = LoveSpotAvailabilityApiStatus.of(loveSpot.availability)
            )
        }
    }
}

enum class LoveSpotAvailabilityApiStatus {
    ALL_DAY, NIGHT_ONLY;

    companion object {
        fun of(availability: LoveSpot.Availability): LoveSpotAvailabilityApiStatus {
            return when (availability) {
                LoveSpot.Availability.ALL_DAY -> ALL_DAY
                LoveSpot.Availability.NIGHT_ONLY -> NIGHT_ONLY
            }
        }
    }

    fun toModel(): LoveSpot.Availability {
        return when (this) {
            ALL_DAY -> LoveSpot.Availability.ALL_DAY
            NIGHT_ONLY -> LoveSpot.Availability.NIGHT_ONLY
        }
    }
}

data class CreateLoveSpotRequest(
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val description: String,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    var availability: LoveSpotAvailabilityApiStatus
)

data class LoveSpotSearchRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)
