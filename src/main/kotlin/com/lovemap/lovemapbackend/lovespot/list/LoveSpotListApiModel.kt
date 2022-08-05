package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.utils.INVALID_DISTANCE_IN_METERS
import com.lovemap.lovemapbackend.utils.INVALID_LIMIT
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
    @field:Range(min = 0, max = MAX_LIMIT.toLong(), message = INVALID_LIMIT)
    val limit: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @field:Range(min = 0, max = 1_000_000, message = INVALID_DISTANCE_IN_METERS)
    val distanceInMeters: Int? = null,
    val locationName: String? = null,
    val typeFilter: List<LoveSpotResponse.Type> = LoveSpotResponse.Type.values().toList()
)

enum class ListOrderingRequest {
    CLOSEST, TOP_RATED, RECENTLY_ACTIVE, POPULAR;

    fun toDto(): ListOrdering {
        return when (this) {
            CLOSEST -> ListOrdering.CLOSEST
            TOP_RATED -> ListOrdering.TOP_RATED
            RECENTLY_ACTIVE -> ListOrdering.RECENTLY_ACTIVE
            POPULAR -> ListOrdering.POPULAR
        }
    }
}

enum class ListLocationRequest {
    COORDINATE, CITY, COUNTRY;

    fun toDto(): ListLocationType {
        return when (this) {
            COORDINATE -> ListLocationType.COORDINATE
            CITY -> ListLocationType.CITY
            COUNTRY -> ListLocationType.COUNTRY
        }
    }
}