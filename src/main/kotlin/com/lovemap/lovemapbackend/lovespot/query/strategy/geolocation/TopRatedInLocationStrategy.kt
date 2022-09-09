package com.lovemap.lovemapbackend.lovespot.query.strategy.geolocation

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.CITY
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType.COUNTRY
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering.TOP_RATED
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotSearchDto
import com.lovemap.lovemapbackend.lovespot.query.strategy.LoveSpotSearchStrategy
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class TopRatedInLocationStrategy(
    private val repository: LoveSpotRepository,
) : LoveSpotSearchStrategy {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(
            Pair(CITY, TOP_RATED),
            Pair(COUNTRY, TOP_RATED),
        )
    }

    override suspend fun listSpots(listDto: LoveSpotSearchDto): List<LoveSpot> {
        val loveSpotFlow = when (listDto.listLocation) {
            ListLocationType.COORDINATE -> throw LoveMapException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.InvalidListLocationType
            )
            CITY -> repository.findByCityOrderByRating(
                city = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
            COUNTRY -> repository.findByCountryOrderByRating(
                country = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
        }

        return loveSpotFlow.toList()
    }
}