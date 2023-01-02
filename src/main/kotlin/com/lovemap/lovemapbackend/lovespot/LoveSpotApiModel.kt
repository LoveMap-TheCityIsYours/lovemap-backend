package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.utils.INVALID_LOVE_DESCRIPTION
import com.lovemap.lovemapbackend.utils.INVALID_LOVE_SPOT_NAME
import java.time.LocalTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class LoveSpotResponse(
    val id: Long,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val description: String,
    val averageRating: Double?,
    val numberOfReports: Int,
    val customAvailability: Pair<LocalTime, LocalTime>?,
    val availability: Availability,
    val averageDanger: Double?,
    val numberOfRatings: Int,
    val addedBy: Long,
    val numberOfPhotos: Int,
    val type: Type = Type.PUBLIC_SPACE,
) {
    companion object {
        fun of(loveSpot: LoveSpot): LoveSpotResponse {
            return LoveSpotResponse(
                id = loveSpot.id,
                name = loveSpot.name,
                addedBy = loveSpot.addedBy,
                longitude = loveSpot.longitude,
                latitude = loveSpot.latitude,
                averageRating = loveSpot.averageRating,
                averageDanger = loveSpot.averageDanger,
                numberOfRatings = loveSpot.numberOfRatings,
                numberOfReports = loveSpot.numberOfReports,
                numberOfPhotos = loveSpot.numberOfPhotos,
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

        fun toEntity(): LoveSpot.Availability {
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

        fun toEntity(): LoveSpot.Type {
            return when (this) {
                PUBLIC_SPACE -> LoveSpot.Type.PUBLIC_SPACE
                SWINGER_CLUB -> LoveSpot.Type.SWINGER_CLUB
                CRUISING_SPOT -> LoveSpot.Type.CRUISING_SPOT
                SEX_BOOTH -> LoveSpot.Type.SEX_BOOTH
                NIGHT_CLUB -> LoveSpot.Type.NIGHT_CLUB
                OTHER_VENUE -> LoveSpot.Type.OTHER_VENUE
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
    @field:Size(min = 5, max = 1000, message = INVALID_LOVE_DESCRIPTION)
    val description: String,
    var customAvailability: Pair<LocalTime, LocalTime>?,
    @field:NotNull
    val availability: LoveSpotResponse.Availability,
    val type: LoveSpotResponse.Type = LoveSpotResponse.Type.PUBLIC_SPACE,
)

data class UpdateLoveSpotRequest(
    val name: String? = null,
    val description: String? = null,
    var availability: LoveSpotResponse.Availability?,
    val type: LoveSpotResponse.Type?,
    val customAvailability: Pair<LocalTime, LocalTime>? = null
)

data class RecommendationsRequest(
    val longitude: Double?,
    val latitude: Double?,
    val country: String,
    val typeFilter: List<LoveSpotResponse.Type> = LoveSpotResponse.Type.values().toList()
)

data class RecommendationsResponse(
    val topRatedSpots: List<LoveSpotResponse>,
    val closestSpots: List<LoveSpotResponse>,
    val recentlyActiveSpots: List<LoveSpotResponse>,
    val popularSpots: List<LoveSpotResponse>,
    val newestSpots: List<LoveSpotResponse>,
)
