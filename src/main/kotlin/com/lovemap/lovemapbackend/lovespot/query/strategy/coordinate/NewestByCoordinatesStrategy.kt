package com.lovemap.lovemapbackend.lovespot.query.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.COORDINATE
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering.NEWEST
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotDistanceSorter
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class NewestByCoordinatesStrategy(
    sorter: LoveSpotDistanceSorter,
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy(sorter) {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(Pair(COORDINATE, NEWEST))
    }

    override suspend fun doListSpots(
        center: LatLng,
        from: LatLng,
        to: LatLng,
        limit: Int,
        typeFilter: Set<LoveSpot.Type>
    ): List<LoveSpot> {
        return repository.findByCoordinatesOrderByNewest(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            typeFilter = typeFilter,
            limit = limit
        ).toList()
    }
}