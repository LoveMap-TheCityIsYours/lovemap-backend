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
    typeFilter: Set<LoveSpot.Type>,
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy(center, distance, limit, typeFilter) {

    override suspend fun listSpots(): Flow<LoveSpot> {
        return repository.findByCoordinatesOrderByRating(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            typeFilter = typeFilter,
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
                typeFilter = request.typeFilter.map { it.toModel() }.toSet(),
                repository = repository
            )
        }
    }
}