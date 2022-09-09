package com.lovemap.lovemapbackend.lovespot.query.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.COORDINATE
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering.CLOSEST
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotDistanceSorter
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClosestByCoordinatesStrategy(
    sorter: LoveSpotDistanceSorter,
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy(sorter) {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(Pair(COORDINATE, CLOSEST))
    }

    override suspend fun doListSpots(
        center: LatLng,
        from: LatLng,
        to: LatLng,
        limit: Int,
        typeFilter: Set<LoveSpot.Type>
    ): List<LoveSpot> {
        return repository.findByCoordinatesOrderByClosest(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            centerLat = center.latitude,
            centerLong = center.longitude,
            typeFilter = typeFilter,
            limit = limit
        ).toList().let {
            sorter.sortList(center, it)
        }
    }
}