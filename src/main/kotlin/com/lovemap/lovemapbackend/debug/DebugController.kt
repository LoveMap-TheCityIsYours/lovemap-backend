package com.lovemap.lovemapbackend.debug

import com.lovemap.lovemapbackend.geolocation.CachedGeoLocationProvider
import com.lovemap.lovemapbackend.geolocation.Cities
import com.lovemap.lovemapbackend.geolocation.Countries
import com.lovemap.lovemapbackend.lovespot.CreateLoveSpotRequest
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse.Availability.ALL_DAY
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse.Availability.NIGHT_ONLY
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse.Type.PUBLIC_SPACE
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.lovespot.query.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.math.floor
import kotlin.random.Random

@Profile("dev")
@RestController
@RequestMapping("/debug")
class DebugController(
    private val loveSpotListService: LoveSpotQueryService,
    private val loveSpotService: LoveSpotService,
    private val cachedGeoLocationProvider: CachedGeoLocationProvider,
    private val environment: Environment,
) {
    @PostMapping("/advancedSearch")
    suspend fun advancedSearch(
        @RequestParam(name = "searchType", required = true) listOrdering: ListOrderingRequest,
        @RequestParam(name = "searchLocation", required = true) listLocation: ListLocationRequest,
        @RequestBody request: LoveSpotSearchRequest
    ): ResponseEntity<List<LoveSpotResponse>> {
        val loveSpots = loveSpotListService.search(listOrdering, listLocation, request)
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
//        val longFrom = 16.0
//        val longTo = 22.0
//        val latFrom = 45.0
//        val latTo = 48.0
        val longFrom = -180.0
        val longTo = 180.0
        val latFrom = -90.0
        val latTo = 90.0
        if (environment.activeProfiles.contains("dev")) {
            for (i in 0 until amount) {
                val random = Random(System.currentTimeMillis())
                val longitude = random.nextDouble(longFrom, longTo)
                val latitude = random.nextDouble(latFrom, latTo)
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
            val loveSpotFlow = loveSpotListService.list(LoveSpotListRequest(latFrom, longFrom, latTo, longTo, 100))
            return ResponseEntity.ok(loveSpotFlow.map { LoveSpotResponse.of(it) }.toList())
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
    }
}