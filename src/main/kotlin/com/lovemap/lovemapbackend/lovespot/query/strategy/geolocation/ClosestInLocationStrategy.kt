package com.lovemap.lovemapbackend.lovespot.query.strategy.geolocation

import com.javadocmd.simplelatlng.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.CITY
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.COUNTRY
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering.CLOSEST
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotSearchDto
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotDistanceSorter
import com.lovemap.lovemapbackend.lovespot.query.strategy.LoveSpotSearchStrategy
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class ClosestInLocationStrategy(
    private val sorter: LoveSpotDistanceSorter,
    private val repository: LoveSpotRepository,
) : LoveSpotSearchStrategy {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(
            Pair(CITY, CLOSEST),
            Pair(COUNTRY, CLOSEST),
        )
    }

    override suspend fun listSpots(listDto: LoveSpotSearchDto): List<LoveSpot> {
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