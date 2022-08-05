package com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType.CITY
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType.COUNTRY
import com.lovemap.lovemapbackend.lovespot.list.ListOrdering
import com.lovemap.lovemapbackend.lovespot.list.ListOrdering.RECENTLY_ACTIVE
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategy
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class RecentlyActiveInLocationStrategy(private val repository: LoveSpotRepository) : LoveSpotListStrategy {

    override fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>> {
        return setOf(
            Pair(CITY, RECENTLY_ACTIVE),
            Pair(COUNTRY, RECENTLY_ACTIVE),
        )
    }

    override suspend fun listSpots(listDto: LoveSpotAdvancedListDto): List<LoveSpot> {
        val loveSpotFlow = when (listDto.listLocation) {
            ListLocationType.COORDINATE -> throw LoveMapException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.InvalidListLocationType
            )
            CITY -> repository.findByCityOrderByRecentlyActive(
                city = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
            COUNTRY -> repository.findByCountryOrderByRecentlyActive(
                country = listDto.locationName!!,
                typeFilter = listDto.typeFilter,
                limit = listDto.limit
            )
        }

        return loveSpotFlow.toList()
    }
}