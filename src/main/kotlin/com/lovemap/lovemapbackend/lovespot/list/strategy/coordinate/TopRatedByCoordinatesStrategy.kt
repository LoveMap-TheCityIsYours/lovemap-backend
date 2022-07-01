package com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotAdvancedListRequest
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import kotlinx.coroutines.flow.Flow

class TopRatedByCoordinatesStrategy(
    center: LatLng,
    distance: Int,
    limit: Int,
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy(center, distance, limit) {

    override suspend fun listSpots(): Flow<LoveSpot> {
        return repository.findByCoordinatesOrderByRating(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            limit = limit
        )
    }

    companion object {
        fun of(
            request: LoveSpotAdvancedListRequest,
            repository: LoveSpotRepository
        ): TopRatedByCoordinatesStrategy {
            return TopRatedByCoordinatesStrategy(
                center = LatLng(request.lat!!, request.long!!),
                distance = request.distanceInMeters!!,
                limit = request.limit,
                repository = repository
            )
        }
    }
}