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
    var availability: Availability,
    var averageDanger: Double?,
    var numberOfRatings: Int,
    var addedBy: Long,
    var type: Type = Type.PUBLIC_SPACE,
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
                availability = Availability.of(loveSpot.availability),
                type = Type.of(loveSpot.type),
            )
        }
    }

    enum class Availability {
        ALL_DAY, NIGHT_ONLY;

        companion object {
            fun of(availability: LoveSpot.Availability): Availability {
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

    enum class Type {
        PUBLIC_SPACE,
        SWINGER_CLUB,
        CRUISING_SPOT,
        SEX_BOOTH,
        NIGHT_CLUB,
        OTHER_VENUE;

        companion object {
            fun of(type: LoveSpot.Type): Type {
                return when (type) {
                    LoveSpot.Type.PUBLIC_SPACE -> PUBLIC_SPACE
                    LoveSpot.Type.SWINGER_CLUB -> SWINGER_CLUB
                    LoveSpot.Type.CRUISING_SPOT -> CRUISING_SPOT
                    LoveSpot.Type.SEX_BOOTH -> SEX_BOOTH
                    LoveSpot.Type.NIGHT_CLUB -> NIGHT_CLUB
                    LoveSpot.Type.OTHER_VENUE -> OTHER_VENUE
                }
            }
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
    var availability: LoveSpotDto.Availability,
    var type: LoveSpotDto.Type = LoveSpotDto.Type.PUBLIC_SPACE,
)

data class UpdateLoveSpotRequest(
    val name: String? = null,
    val description: String? = null,
    var availability: LoveSpotDto.Availability,
    var customAvailability: Pair<LocalTime, LocalTime>? = null
)

data class LoveSpotListRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)

data class LoveSpotSearchRequest(
    @field:NotNull
    val limit: Int,
    val lat: Double? = null,
    val long: Double? = null,
    val distance: Int? = null,
    val locationName: String? = null,
    val activityType: ActivityType? = null,
)

enum class SearchType {
    CLOSEST, BEST, ACTIVE
}

enum class SearchLocation {
    COORDINATE, CITY, COUNTRY
}

enum class ActivityType {
    MADE_LOVE, COMMENTED
}
