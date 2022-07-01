package com.lovemap.lovemapbackend.lovespot.list

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.*
import com.lovemap.lovemapbackend.lovespot.ListOrdering.*
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategyFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import kotlin.math.sqrt

@Service
class LoveSpotListService(
    private val loveSpotListStrategyFactory: LoveSpotListStrategyFactory,
    private val repository: LoveSpotRepository,
) {
    private val maxListLimit = 100

    suspend fun list(request: LoveSpotListRequest): Flow<LoveSpot> {
        return repository.findByCoordinatesOrderByRating(
            longFrom = request.longFrom,
            longTo = request.longTo,
            latFrom = request.latFrom,
            latTo = request.latTo,
            limit = if (request.limit <= maxListLimit) request.limit else maxListLimit
        )
    }

    suspend fun advancedList(
        listOrdering: ListOrdering,
        listLocation: ListLocation,
        request: LoveSpotAdvancedListRequest
    ): Flow<LoveSpot> {
        val listStrategy = loveSpotListStrategyFactory.getListStrategy(listOrdering, listLocation, request)
        return listStrategy.listSpots()
    }
}