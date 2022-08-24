package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.LoveSpot

data class LoveSpotAdvancedListDto(
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
    CLOSEST, TOP_RATED, RECENTLY_ACTIVE, POPULAR, NEWEST
}

enum class ListLocationType {
    COORDINATE, CITY, COUNTRY
}