package com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType.CITY
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType.COUNTRY
import com.lovemap.lovemapbackend.lovespot.list.ListOrdering
import com.lovemap.lovemapbackend.lovespot.list.ListOrdering.CLOSEST
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotDistanceSorter
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategy
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ClosestInLocationStrategy(
    private val sorter: LoveSpotDistanceSorter,
    private val repository: LoveSpotRepository,
) : LoveSpotListStrategy {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(
            Pair(CITY, CLOSEST),
            Pair(COUNTRY, CLOSEST),
        )
    }

    override suspend fun listSpots(listDto: LoveSpotAdvancedListDto): List<LoveSpot> {
        val loveSpotFlow = when (listDto.listLocation) {
            ListLocationType.COORDINATE -> throw LoveMapException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.InvalidListLocationType
            )
            CITY -> repository.findByCityOrderByClosest(
                centerLat = listDto.latitude!!,
                centerLong = listDto.longitude!!,
                city = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
            COUNTRY -> repository.findByCountryOrderByClosest(
                centerLat = listDto.latitude!!,
                centerLong = listDto.longitude!!,
                country = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
        }

        return loveSpotFlow.toList().let {
            sorter.sortList(LatLng(listDto.latitude, listDto.longitude), it)
        }
    }
}