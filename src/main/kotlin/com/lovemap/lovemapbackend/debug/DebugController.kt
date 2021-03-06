package com.lovemap.lovemapbackend.debug

import com.lovemap.lovemapbackend.geolocation.Cities
import com.lovemap.lovemapbackend.geolocation.Countries
import com.lovemap.lovemapbackend.geolocation.GeoLocationRepository
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.list.ListLocationRequest
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingRequest
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListRequest
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotListService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Profile("dev")
@RestController
@RequestMapping("/debug")
class DebugController(
    private val loveSpotListService: LoveSpotListService,
    private val geoLocationService: GeoLocationService,
    private val geoLocationRepository: GeoLocationRepository
) {
    @PostMapping("/advancedSearch")
    suspend fun advancedSearch(
        @RequestParam(name = "searchType", required = true) listOrdering: ListOrderingRequest,
        @RequestParam(name = "searchLocation", required = true) listLocation: ListLocationRequest,
        @RequestBody request: LoveSpotAdvancedListRequest
    ): ResponseEntity<Flow<LoveSpotResponse>> {
        val loveSpots = loveSpotListService.advancedList(listOrdering, listLocation, request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotResponse.of(it) })
    }

    @GetMapping("/countries")
    suspend fun getCountries(): ResponseEntity<Countries> {
        return ResponseEntity.ok(geoLocationService.findAllCountries())
    }

    @GetMapping("/cities")
    suspend fun getCities(): ResponseEntity<Cities> {
        return ResponseEntity.ok(geoLocationService.findAllCities())
    }
}