package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.stereotype.Service

@Service
class LoveSpotListService(
    loveSpotListStrategies: List<LoveSpotListStrategy>,
    private val loveSpotListValidator: LoveSpotListValidator,
    private val repository: LoveSpotRepository,
) {
    private val maxListLimit = 100
    private val strategies = HashMap<Pair<ListLocationDto, ListOrderingDto>, LoveSpotListStrategy>()

    init {
        loveSpotListStrategies.forEach { strategy ->
            strategy.getSupportedConditions().forEach { condition ->
                strategies[condition] = strategy
            }
        }
    }

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
        listOrdering: ListOrderingRequest,
        listLocation: ListLocationRequest,
        request: LoveSpotAdvancedListRequest
    ): Flow<LoveSpot> {
        val listDto = loveSpotListValidator.validateAndConvertRequest(listOrdering, listLocation, request)
        val strategyCondition = Pair(listDto.listLocation, listDto.listOrdering)
        return strategies[strategyCondition]?.listSpots(listDto) ?: emptyFlow()
    }
}