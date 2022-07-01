package com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotAdvancedListRequest
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class ClosestInLocationStrategy(
    private val center: LatLng,
    private val locationName: String,
    private val limit: Int,
    private val geoLocationProvider: (name: String) -> Flow<GeoLocation>,
    private val repository: LoveSpotRepository,
) : LoveSpotListStrategy {

    override suspend fun listSpots(): Flow<LoveSpot> {
        val geoLocations = geoLocationProvider(locationName)
        return repository.findByGeoLocOrderByClosest(
            centerLat = center.latitude,
            centerLong = center.longitude,
            geoLocationIds = geoLocations.map { it.id }.toList(),
            limit = limit
        )
    }

    companion object {
        fun of(
            request: LoveSpotAdvancedListRequest,
            geoLocationProvider: (name: String) -> Flow<GeoLocation>,
            repository: LoveSpotRepository
        ): ClosestInLocationStrategy {
            return ClosestInLocationStrategy(
                center = LatLng(request.lat!!, request.long!!),
                locationName = request.locationName!!,
                limit = request.limit,
                geoLocationProvider = geoLocationProvider,
                repository = repository
            )
        }
    }
}