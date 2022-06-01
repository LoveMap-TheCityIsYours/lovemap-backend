package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.utils.INVALID_LOVE_DESCRIPTION
import com.lovemap.lovemapbackend.utils.INVALID_LOVE_SPOT_NAME
import java.time.LocalTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

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
    var addedBy: Long,
) {
    companion object {
        fun of(loveSpot: LoveSpot): LoveSpotDto {
            return LoveSpotDto(
                id = loveSpot.id,
                name = loveSpot.name,
                addedBy = loveSpot.addedBy,
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
    @field:Size(min = 3, max = 50, message = INVALID_LOVE_SPOT_NAME)
    val name: String,
    @field:NotNull
    val longitude: Double,
    @field:NotNull
    val latitude: Double,
    @field:Size(min = 5, max = 250, message = INVALID_LOVE_DESCRIPTION)
    val description: String,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    @field:NotEmpty
    var availability: LoveSpotAvailabilityApiStatus
)

data class UpdateLoveSpotRequest(
    val name: String? = null,
    val description: String? = null,
    var availability: LoveSpotAvailabilityApiStatus,
    var customAvailability: Pair<LocalTime, LocalTime>? = null
)

data class LoveSpotSearchRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)
