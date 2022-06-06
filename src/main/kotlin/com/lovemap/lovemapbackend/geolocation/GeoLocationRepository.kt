package com.lovemap.lovemapbackend.geolocation

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface GeoLocationRepository : CoroutineCrudRepository<GeoLocation, Long> {
    suspend fun findByPostalCodeAndCityAndCountyAndCountry(
        postalCode: String?,
        city: String?,
        county: String?,
        country: String?
    ): GeoLocation?
}