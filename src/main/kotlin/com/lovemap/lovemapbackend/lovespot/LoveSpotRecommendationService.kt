package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.lovespot.list.ListLocationRequest.COUNTRY
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingRequest.*
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListRequest
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotListService
import org.springframework.stereotype.Service

@Service
class LoveSpotRecommendationService(
    private val loveSpotListService: LoveSpotListService
) {

    suspend fun getRecommendations(request: RecommendationsRequest): RecommendationsResponse {
        val listRequest = LoveSpotAdvancedListRequest(
            limit = 2,
            latitude = request.latitude,
            longitude = request.longitude,
            locationName = request.country,
            typeFilter = request.typeFilter
        )
        return RecommendationsResponse(
            topRatedSpots = getTopRatedSpots(listRequest),
            closestSpots = getClosestSpots(request, listRequest),
            recentlyActiveSpots = getRecentlyActiveSpots(listRequest),
            popularSpots = getPopularSpots(listRequest),
            newestSpots = getNewestSpots(listRequest)
        )
    }

    private suspend fun getTopRatedSpots(listRequest: LoveSpotAdvancedListRequest) =
        loveSpotListService.advancedList(TOP_RATED, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }

    private suspend fun getClosestSpots(
        request: RecommendationsRequest,
        listRequest: LoveSpotAdvancedListRequest
    ) = if (request.latitude != null && request.longitude != null) {
        loveSpotListService.advancedList(CLOSEST, COUNTRY, listRequest).map { LoveSpotResponse.of(it) }
    } else {
        emptyList()
    }

    private suspend fun getRecentlyActiveSpots(listRequest: LoveSpotAdvancedListRequest) =
        loveSpotListService.advancedList(RECENTLY_ACTIVE, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }

    private suspend fun getPopularSpots(listRequest: LoveSpotAdvancedListRequest) =
        loveSpotListService.advancedList(POPULAR, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }

    private suspend fun getNewestSpots(listRequest: LoveSpotAdvancedListRequest): List<LoveSpotResponse> {
        return loveSpotListService.advancedList(NEWEST, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }
    }
}