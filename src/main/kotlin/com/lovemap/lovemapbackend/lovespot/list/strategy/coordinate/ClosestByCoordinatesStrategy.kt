package com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto.COORDINATE
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto.CLOSEST
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
class ClosestByCoordinatesStrategy(
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy() {

    override fun getSupportedConditions(): Set<Pair<ListLocationDto, ListOrderingDto>> {
        return setOf(Pair(COORDINATE, CLOSEST))
    }

    override suspend fun doListSpots(
        center: LatLng,
        from: LatLng,
        to: LatLng,
        limit: Int,
        typeFilter: Set<LoveSpot.Type>
    ): Flow<LoveSpot> {
        return repository.findByCoordinatesOrderByClosest(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            centerLat = center.latitude,
            centerLong = center.longitude,
            typeFilter = typeFilter,
            limit = limit
        )
    }
}