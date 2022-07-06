package com.lovemap.lovemapbackend.debug

import com.lovemap.lovemapbackend.geolocation.Cities
import com.lovemap.lovemapbackend.geolocation.Countries
import com.lovemap.lovemapbackend.geolocation.GeoLocationRepository
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.ListLocation
import com.lovemap.lovemapbackend.lovespot.ListOrdering
import com.lovemap.lovemapbackend.lovespot.LoveSpotAdvancedListRequest
import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
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
        @RequestParam(name = "searchType", required = true) listOrdering: ListOrdering,
        @RequestParam(name = "searchLocation", required = true) listLocation: ListLocation,
        @RequestBody request: LoveSpotAdvancedListRequest
    ): ResponseEntity<Flow<LoveSpotDto>> {
        val loveSpots = loveSpotListService.advancedList(listOrdering, listLocation, request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotDto.of(it) })
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