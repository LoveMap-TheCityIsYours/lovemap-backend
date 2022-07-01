package com.lovemap.lovemapbackend.geolocation

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface GeoLocationRepository : CoroutineCrudRepository<GeoLocation, Long> {
    suspend fun findByPostalCodeAndCityAndCountyAndCountry(
        postalCode: String?,
        city: String?,
        county: String?,
        country: String?
    ): GeoLocation?

    fun findByCountry(country: String): Flow<GeoLocation>

    fun findByCity(city: String): Flow<GeoLocation>
}