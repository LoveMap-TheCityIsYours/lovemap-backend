package com.lovemap.lovemapbackend.lovespot.query

import com.lovemap.lovemapbackend.lovespot.LoveSpot

data class LoveSpotSearchDto(
    val limit: Int,
    val typeFilter: Set<LoveSpot.Type>,
    val listOrdering: ListOrdering,
    val listLocation: ListLocationType,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceInMeters: Int? = null,
    val locationName: String? = null
)

enum class ListOrdering {
    CLOSEST, TOP_RATED, RECENTLY_ACTIVE, POPULAR, NEWEST, RECENT_PHOTOS
}

enum class ListLocationType {
    COORDINATE, CITY, COUNTRY
}