package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.utils.INVALID_DISTANCE_IN_METERS
import org.hibernate.validator.constraints.Range
import javax.validation.constraints.NotNull

data class LoveSpotListRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)

data class LoveSpotAdvancedListRequest(
    @field:NotNull
    val limit: Int,
    val lat: Double? = null,
    val long: Double? = null,
    @field:Range(min = 1, max = 1_000_000, message = INVALID_DISTANCE_IN_METERS)
    val distanceInMeters: Int? = null,
    val locationName: String? = null,
    val typeFilter: List<LoveSpotResponse.Type> = LoveSpotResponse.Type.values().toList()
)

enum class ListOrderingRequest {
    CLOSEST, TOP_RATED, RECENTLY_ACTIVE, POPULAR;

    fun toDto(): ListOrderingDto {
        return when (this) {
            CLOSEST -> ListOrderingDto.CLOSEST
            TOP_RATED -> ListOrderingDto.TOP_RATED
            RECENTLY_ACTIVE -> ListOrderingDto.RECENTLY_ACTIVE
            POPULAR -> ListOrderingDto.POPULAR
        }
    }
}

enum class ListLocationRequest {
    COORDINATE, CITY, COUNTRY;

    fun toDto(): ListLocationDto {
        return when (this) {
            COORDINATE -> ListLocationDto.COORDINATE
            CITY -> ListLocationDto.CITY
            COUNTRY -> ListLocationDto.COUNTRY
        }
    }
}