package com.lovemap.lovemapbackend.debug

import com.lovemap.lovemapbackend.geolocation.CachedGeoLocationProvider
import com.lovemap.lovemapbackend.geolocation.Cities
import com.lovemap.lovemapbackend.geolocation.Countries
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.CreateLoveSpotRequest
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse.Availability.ALL_DAY
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse.Availability.NIGHT_ONLY
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse.Type.PUBLIC_SPACE
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.list.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import kotlin.math.floor
import kotlin.random.Random

@Profile("dev")
@RestController
@RequestMapping("/debug")
class DebugController(
    private val loveSpotListService: LoveSpotListService,
    private val loveSpotService: LoveSpotService,
    private val cachedGeoLocationProvider: CachedGeoLocationProvider,
    private val environment: Environment,
) {
    @PostMapping("/advancedSearch")
    suspend fun advancedSearch(
        @RequestParam(name = "searchType", required = true) listOrdering: ListOrderingRequest,
        @RequestParam(name = "searchLocation", required = true) listLocation: ListLocationRequest,
        @RequestBody request: LoveSpotAdvancedListRequest
    ): ResponseEntity<List<LoveSpotResponse>> {
        val loveSpots = loveSpotListService.advancedList(listOrdering, listLocation, request)
        return ResponseEntity.ok(loveSpots.map { LoveSpotResponse.of(it) })
    }

    @GetMapping("/countries")
    suspend fun getCountries(): ResponseEntity<Countries> {
        return ResponseEntity.ok(cachedGeoLocationProvider.findAllCountries())
    }

    @GetMapping("/cities")
    suspend fun getCities(): ResponseEntity<Cities> {
        return ResponseEntity.ok(cachedGeoLocationProvider.findAllCities())
    }

    @PostMapping("/createSpots")
    suspend fun createSpots(@RequestParam amount: Int): ResponseEntity<List<LoveSpotResponse>> {
        // longitude: 16 - 22
        // latitude: 45 - 48
        if (environment.activeProfiles.contains("dev")) {
            val random = Random(System.currentTimeMillis())
            for (i in 0 until amount) {
                val longitude = random.nextDouble(0.0, 90.0)
                val latitude = random.nextDouble(0.0, 90.0)
                val name = UUID.randomUUID().toString()
                loveSpotService.create(
                    CreateLoveSpotRequest(
                        name,
                        longitude,
                        latitude,
                        name,
                        null,
                        if (floor(latitude).toInt() % 2 == 0) {
                            ALL_DAY
                        } else {
                            NIGHT_ONLY
                        },
                        PUBLIC_SPACE
                    )
                )
            }
            val loveSpotFlow = loveSpotListService.list(LoveSpotListRequest(45.0, 16.0, 48.0, 22.0, 100))
            return ResponseEntity.ok(loveSpotFlow.map { LoveSpotResponse.of(it) }.toList())
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }
}