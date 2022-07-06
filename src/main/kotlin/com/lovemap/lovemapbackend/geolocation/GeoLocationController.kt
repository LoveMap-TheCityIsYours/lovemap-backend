package com.lovemap.lovemapbackend.geolocation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/geolocations")
class GeoLocationController(
    private val geoLocationService: GeoLocationService
) {

    @GetMapping("/countries")
    suspend fun getCountries(): ResponseEntity<Countries> {
        return ResponseEntity.ok(geoLocationService.findAllCountries())
    }

    @GetMapping("/cities")
    suspend fun getCities(): ResponseEntity<Cities> {
        return ResponseEntity.ok(geoLocationService.findAllCities())
    }
}
