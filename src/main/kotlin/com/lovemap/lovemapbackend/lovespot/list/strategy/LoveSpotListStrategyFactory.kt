package com.lovemap.lovemapbackend.lovespot.list.strategy

import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.*
import com.lovemap.lovemapbackend.lovespot.ListOrdering.*
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotListValidator
import com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate.ClosestByCoordinatesStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate.PopularByCoordinatesStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate.RecentlyActiveByCoordinatesStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate.TopRatedByCoordinatesStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation.ClosestInLocationStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation.PopularInLocationStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation.RecentlyActiveInLocationStrategy
import com.lovemap.lovemapbackend.lovespot.list.strategy.geolocation.TopRatedInLocationStrategy
import org.springframework.stereotype.Service

@Service
class LoveSpotListStrategyFactory(
    private val loveSpotListValidator: LoveSpotListValidator,
    private val geoLocationService: GeoLocationService,
    private val repository: LoveSpotRepository,
) {
    fun getListStrategy(
        listOrdering: ListOrdering,
        listLocation: ListLocation,
        request: LoveSpotAdvancedListRequest
    ): LoveSpotListStrategy {
        loveSpotListValidator.validateRequest(listOrdering, listLocation, request)
        val preparedRequest = prepareRequest(request)
        return when (listLocation) {
            ListLocation.COORDINATE -> byCoordinate(listOrdering, preparedRequest)
            ListLocation.CITY -> byCity(listOrdering, preparedRequest)
            ListLocation.COUNTRY -> byCountry(listOrdering, preparedRequest)
        }
    }

    private fun prepareRequest(request: LoveSpotAdvancedListRequest): LoveSpotAdvancedListRequest {
        val actualRequest = if (request.typeFilter.isEmpty()) {
            request.copy(typeFilter = LoveSpotDto.Type.values().toList())
        } else {
            request
        }
        return actualRequest
    }

    private fun byCoordinate(listOrdering: ListOrdering, request: LoveSpotAdvancedListRequest): LoveSpotListStrategy {
        return when (listOrdering) {
            CLOSEST -> ClosestByCoordinatesStrategy.of(request, repository)
            TOP_RATED -> TopRatedByCoordinatesStrategy.of(request, repository)
            RECENTLY_ACTIVE -> RecentlyActiveByCoordinatesStrategy.of(request, repository)
            POPULAR -> PopularByCoordinatesStrategy.of(request, repository)
        }
    }

    private fun byCity(listOrdering: ListOrdering, request: LoveSpotAdvancedListRequest): LoveSpotListStrategy {
        return when (listOrdering) {
            CLOSEST -> ClosestInLocationStrategy.of(request, byCity(), repository)
            TOP_RATED -> TopRatedInLocationStrategy.of(request, byCity(), repository)
            RECENTLY_ACTIVE -> RecentlyActiveInLocationStrategy.of(request, byCity(), repository)
            POPULAR -> PopularInLocationStrategy.of(request, byCity(), repository)
        }
    }

    private fun byCountry(listOrdering: ListOrdering, request: LoveSpotAdvancedListRequest): LoveSpotListStrategy {
        return when (listOrdering) {
            CLOSEST -> ClosestInLocationStrategy.of(request, byCountry(), repository)
            TOP_RATED -> TopRatedInLocationStrategy.of(request, byCountry(), repository)
            RECENTLY_ACTIVE -> RecentlyActiveInLocationStrategy.of(request, byCountry(), repository)
            POPULAR -> PopularInLocationStrategy.of(request, byCountry(), repository)
        }
    }

    private fun byCountry() = { name: String -> geoLocationService.listByCountry(name) }

    private fun byCity() = { name: String -> geoLocationService.listByCity(name) }
}