package com.lovemap.lovemapbackend.lovespot

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.lovespot.query.ListLocationRequest.COUNTRY
import com.lovemap.lovemapbackend.lovespot.query.ListOrderingRequest.*
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotQueryService
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotSearchRequest
import com.lovemap.lovemapbackend.tracking.UserTrackingService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class LoveSpotRecommendationService(
    private val authorizationService: AuthorizationService,
    private val loveSpotListService: LoveSpotQueryService,
    private val userTrackingService: UserTrackingService,
) {
    private val logger = KotlinLogging.logger {}

    private val recommendationSize = 5

    suspend fun getRecommendations(request: RecommendationsRequest): RecommendationsResponse {
        val listRequest = LoveSpotSearchRequest(
            limit = recommendationSize,
            latitude = request.latitude,
            longitude = request.longitude,
            locationName = request.country,
            typeFilter = request.typeFilter
        )
        userTrackingService.trackLocation(
            caller = authorizationService.getCaller(),
            latitude = request.latitude,
            longitude = request.longitude
        )
        return RecommendationsResponse(
            topRatedSpots = getTopRatedSpots(listRequest),
            closestSpots = getClosestSpots(request, listRequest),
            recentlyActiveSpots = getRecentlyActiveSpots(listRequest),
            popularSpots = getPopularSpots(listRequest),
            newestSpots = getNewestSpots(listRequest),
            recentPhotoSpots = getRecentPhotoSpots(listRequest)
        )
    }

    private suspend fun getTopRatedSpots(listRequest: LoveSpotSearchRequest) =
        loveSpotListService.search(TOP_RATED, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }

    private suspend fun getClosestSpots(
        request: RecommendationsRequest,
        listRequest: LoveSpotSearchRequest
    ) = if (request.latitude != null && request.longitude != null) {
        loveSpotListService.search(CLOSEST, COUNTRY, listRequest).map { LoveSpotResponse.of(it) }
    } else {
        emptyList()
    }

    private suspend fun getRecentlyActiveSpots(listRequest: LoveSpotSearchRequest) =
        loveSpotListService.search(RECENTLY_ACTIVE, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }

    private suspend fun getPopularSpots(listRequest: LoveSpotSearchRequest) =
        loveSpotListService.search(POPULAR, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }

    private suspend fun getNewestSpots(listRequest: LoveSpotSearchRequest): List<LoveSpotResponse> {
        return loveSpotListService.search(NEWEST, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }
    }

    private suspend fun getRecentPhotoSpots(listRequest: LoveSpotSearchRequest): List<LoveSpotResponse> {
        return loveSpotListService.search(RECENT_PHOTOS, COUNTRY, listRequest)
            .map { LoveSpotResponse.of(it) }
    }

}
