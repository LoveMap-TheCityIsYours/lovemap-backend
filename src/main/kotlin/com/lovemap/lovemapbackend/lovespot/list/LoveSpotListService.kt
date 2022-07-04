package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.*
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategyFactory
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

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
            typeFilter = LoveSpot.Type.values().toSet(),
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