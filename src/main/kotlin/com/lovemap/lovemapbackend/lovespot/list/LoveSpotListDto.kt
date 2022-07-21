package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.LoveSpot

data class LoveSpotAdvancedListDto(
    val limit: Int,
    val typeFilter: Set<LoveSpot.Type>,
    val listOrdering: ListOrderingDto,
    val listLocation: ListLocationDto,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distanceInMeters: Int? = null,
    val locationName: String? = null
)

enum class ListOrderingDto {
    CLOSEST, TOP_RATED, RECENTLY_ACTIVE, POPULAR
}

enum class ListLocationDto {
    COORDINATE, CITY, COUNTRY
}