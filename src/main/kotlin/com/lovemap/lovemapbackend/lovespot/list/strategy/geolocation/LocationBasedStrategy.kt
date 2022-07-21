package com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation

import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList

abstract class LocationBasedStrategy(
    private val geoLocationService: GeoLocationService
) : LoveSpotListStrategy {

    final override suspend fun listSpots(listDto: LoveSpotAdvancedListDto): Flow<LoveSpot> {
        val geoLocations = when (listDto.listLocation) {
            ListLocationDto.CITY -> getCityLocations(listDto.locationName!!)
            ListLocationDto.COUNTRY -> getCountryLocations(listDto.locationName!!)
            ListLocationDto.COORDINATE ->
                throw IllegalArgumentException("${ListLocationDto.COORDINATE} is not supported here.")
        }.toList()
        if (geoLocations.isEmpty()) {
            return emptyFlow()
        }
        return doListSpots(geoLocations, listDto)
    }

    abstract suspend fun doListSpots(
        geoLocations: List<GeoLocation>,
        listDto: LoveSpotAdvancedListDto,
    ): Flow<LoveSpot>

    private fun getCountryLocations(name: String): Flow<GeoLocation> {
        return geoLocationService.listByCountry(name)
    }

    private fun getCityLocations(name: String): Flow<GeoLocation> {
        return geoLocationService.listByCity(name)
    }
}