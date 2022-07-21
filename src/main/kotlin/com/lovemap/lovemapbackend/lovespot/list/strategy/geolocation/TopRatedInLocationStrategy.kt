package com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation

import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto.CITY
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto.COUNTRY
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto.TOP_RATED
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component

@Component
class TopRatedInLocationStrategy(
    geoLocationService: GeoLocationService,
    private val repository: LoveSpotRepository,
) : LocationBasedStrategy(geoLocationService) {

    override fun getSupportedConditions(): Set<Pair<ListLocationDto, ListOrderingDto>> {
        return setOf(
            Pair(CITY, TOP_RATED),
            Pair(COUNTRY, TOP_RATED),
        )
    }

    override suspend fun doListSpots(
        geoLocations: List<GeoLocation>,
        listDto: LoveSpotAdvancedListDto
    ): Flow<LoveSpot> {
        return repository.findByGeoLocOrderByRating(
            geoLocationIds = geoLocations.map { it.id },
            typeFilter = listDto.typeFilter,
            limit = listDto.limit
        )
    }
}