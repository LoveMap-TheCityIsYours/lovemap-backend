package com.lovemap.lovemapbackend.geolocation

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface GeoLocationRepository : CoroutineCrudRepository<GeoLocation, Long> {
    suspend fun findByPostalCodeAndCityAndCountyAndCountry(
        postalCode: String?,
        city: String?,
        county: String?,
        country: String?
    ): GeoLocation?

    // TODO: use projections: https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/#projections
    fun findByCountry(country: String): Flow<GeoLocation>

    fun findByCity(city: String): Flow<GeoLocation>

    fun findByCityAndCountry(city: String, country: String): Flow<GeoLocation>

    @Query(
        """
            SELECT DISTINCT country FROM geo_location 
            WHERE country IS DISTINCT FROM 'unknown'
        """
    )
    fun findAllCountries(): Flow<String>

    @Query(
        """
            SELECT DISTINCT ON (city, country) * 
            FROM geo_location  
            WHERE country IS DISTINCT FROM 'unknown' 
            AND country IS NOT NULL 
            AND city IS NOT NULL 
        """
    )
    fun findAllCities(): Flow<GeoLocation>

    @Query(
        """
            SELECT DISTINCT ON (country) * 
            FROM geo_location 
            WHERE country IS NOT NULL 
        """
    )
    fun findAllDistinctCountries(): Flow<GeoLocation>
}