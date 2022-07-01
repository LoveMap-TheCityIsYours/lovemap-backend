package com.lovemap.lovemapbackend.lovespot

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotRepository : CoroutineSortingRepository<LoveSpot, Long> {

    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) AND 
            latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            ORDER BY average_rating DESC NULLS LAST LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByRating(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) AND 
            latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            ORDER BY |/((latitude-:centerLat)^2 + (longitude-:centerLong)^2) ASC LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByClosest(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        centerLat: Double,
        centerLong: Double,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) AND 
            latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            ORDER BY last_active_at DESC NULLS LAST :limit
        """
    )
    fun findByCoordinatesOrderByRecentlyActive(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) AND 
            latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            ORDER BY popularity DESC :limit
        """
    )
    fun findByCoordinatesOrderByPopularity(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            geo_location_id IN (:geoLocationIds)
            ORDER BY average_rating DESC NULLS LAST LIMIT :limit
        """
    )
    fun findByGeoLocOrderByRating(
        geoLocationIds: List<Long>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            geo_location_id IN (:geoLocationIds)
            ORDER BY |/((latitude-:centerLat)^2 + (longitude-:centerLong)^2) ASC LIMIT :limit
        """
    )
    fun findByGeoLocOrderByClosest(
        centerLat: Double,
        centerLong: Double,
        geoLocationIds: List<Long>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            geo_location_id IN (:geoLocationIds)
            ORDER BY last_active_at DESC NULLS LAST :limit
        """
    )
    fun findByGeoLocOrderByRecentlyActive(
        geoLocationIds: List<Long>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location WHERE 
            geo_location_id IN (:geoLocationIds)
            ORDER BY popularity DESC :limit
        """
    )
    fun findByGeoLocOrderByPopularity(
        geoLocationIds: List<Long>,
        limit: Int
    ): Flow<LoveSpot>
}
