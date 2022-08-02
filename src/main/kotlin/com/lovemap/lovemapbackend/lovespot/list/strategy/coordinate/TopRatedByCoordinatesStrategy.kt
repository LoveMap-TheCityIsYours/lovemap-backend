package com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto.COORDINATE
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto.TOP_RATED
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotDistanceSorter
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class TopRatedByCoordinatesStrategy(
    sorter: LoveSpotDistanceSorter,
    private val repository: LoveSpotRepository
) : CoordinateBasedStrategy(sorter) {

    override fun getSupportedConditions(): Set<Pair<ListLocationDto, ListOrderingDto>> {
        return setOf(Pair(COORDINATE, TOP_RATED))
    }

    override suspend fun doListSpots(
        center: LatLng,
        from: LatLng,
        to: LatLng,
        limit: Int,
        typeFilter: Set<LoveSpot.Type>
    ): List<LoveSpot> {
        return repository.findByCoordinatesOrderByRating(
            latFrom = from.latitude,
            longFrom = from.longitude,
            latTo = to.latitude,
            longTo = to.longitude,
            typeFilter = typeFilter,
            limit = limit
        ).toList()
    }
}