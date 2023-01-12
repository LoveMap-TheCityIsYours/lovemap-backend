package com.lovemap.lovemapbackend.lovespot.query.strategy.geolocation

import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.CITY
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.COUNTRY
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering.NEWEST
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotSearchDto
import com.lovemap.lovemapbackend.lovespot.query.strategy.LoveSpotSearchStrategy
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class NewestInLocationStrategy(
    private val repository: LoveSpotRepository,
) : LoveSpotSearchStrategy {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(
            Pair(CITY, NEWEST),
            Pair(COUNTRY, NEWEST),
        )
    }

    override suspend fun listSpots(listDto: LoveSpotSearchDto): List<LoveSpot> {
        val loveSpotFlow = when (listDto.listLocation) {
            ListLocationType.COORDINATE -> throw LoveMapException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.InvalidListLocationType
            )
            CITY -> repository.findByCityOrderByNewest(
                city = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
            COUNTRY -> {
                if (GeoLocation.GLOBAL_LOCATION.equals(listDto.locationName, true)) {
                    repository.findAllOrderByNewest(
                        typeFilter = listDto.typeFilter,
                        limit = listDto.limit
                    )
                } else {
                    repository.findByCountryOrderByNewest(
                        country = listDto.locationName!!,
                        typeFilter = listDto.typeFilter,
                        limit = listDto.limit
                    )
                }
            }
        }

        return loveSpotFlow.toList()
    }
}