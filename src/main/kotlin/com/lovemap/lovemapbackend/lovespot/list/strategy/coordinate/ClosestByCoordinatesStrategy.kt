package com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.LoveSpotAdvancedListRequest
import kotlinx.coroutines.flow.Flow

class ClosestByCoordinatesStrategy(
    private val center: LatLng,
    distance: Int,
    limit: Int,
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy(center, distance, limit) {

    override suspend fun listSpots(): Flow<LoveSpot> {
        return repository.findByCoordinatesOrderByClosest(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            centerLat = center.latitude,
            centerLong = center.longitude,
            limit = limit
        )
    }

    companion object {
        fun of(
            request: LoveSpotAdvancedListRequest,
            repository: LoveSpotRepository
        ): ClosestByCoordinatesStrategy {
            return ClosestByCoordinatesStrategy(
                center = LatLng(request.lat!!, request.long!!),
                distance = request.distanceInMeters!!,
                limit = request.limit,
                repository = repository
            )
        }
    }
}