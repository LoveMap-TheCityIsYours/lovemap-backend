package com.lovemap.lovemapbackend.lovespot.query

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotRepository
import com.lovemap.lovemapbackend.lovespot.query.strategy.LoveSpotSearchStrategy
import com.lovemap.lovemapbackend.utils.ValidatorService
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

@Service
class LoveSpotQueryService(
    loveSpotListStrategies: List<LoveSpotSearchStrategy>,
    private val validatorService: ValidatorService,
    private val loveSpotListValidator: LoveSpotSearchConverter,
    private val repository: LoveSpotRepository,
) {
    private val maxListLimit = MAX_LIMIT_LIST
    private val strategies = HashMap<Pair<ListLocationType, ListOrdering>, LoveSpotSearchStrategy>()

    init {
        loveSpotListStrategies.forEach { strategy ->
            strategy.getSupportedConditions().forEach { condition ->
                strategies[condition] = strategy
            }
        }
    }

    suspend fun list(request: LoveSpotListRequest): Flow<LoveSpot> {
        validatorService.validate(request)
        return repository.findByCoordinatesOrderByRandom(
            longFrom = request.longFrom,
            longTo = request.longTo,
            latFrom = request.latFrom,
            latTo = request.latTo,
            typeFilter = LoveSpot.Type.values().toSet(),
            limit = if (request.limit <= maxListLimit) request.limit else maxListLimit
//            limit = maxListLimit
        )
    }

    suspend fun search(
        listOrdering: ListOrderingRequest,
        listLocation: ListLocationRequest,
        request: LoveSpotSearchRequest
    ): List<LoveSpot> {
        val listDto = loveSpotListValidator.validateAndConvertRequest(listOrdering, listLocation, request)
        val strategyCondition = Pair(listDto.listLocation, listDto.listOrdering)
        return strategies[strategyCondition]?.listSpots(listDto) ?: emptyList()
    }
}