package com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation

import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto.CITY
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto.COUNTRY
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto.POPULAR
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class PopularInLocationStrategy(
    geoLocationService: GeoLocationService,
    private val repository: LoveSpotRepository,
) : LocationBasedStrategy(geoLocationService) {

    override fun getSupportedConditions(): Set<Pair<ListLocationDto, ListOrderingDto>> {
        return setOf(
            Pair(CITY, POPULAR),
            Pair(COUNTRY, POPULAR),
        )
    }

    override suspend fun doListSpots(
        geoLocations: List<GeoLocation>,
        listDto: LoveSpotAdvancedListDto
    ): List<LoveSpot> {
        return repository.findByGeoLocOrderByPopularity(
            geoLocationIds = geoLocations.map { it.id },
            typeFilter = listDto.typeFilter,
            limit = listDto.limit
        ).toList()
    }
}