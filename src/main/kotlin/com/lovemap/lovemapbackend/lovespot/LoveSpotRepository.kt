package com.lovemap.lovemapbackend.lovespot

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotRepository : CoroutineSortingRepository<LoveSpot, Long> {

    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo)
             AND 
            latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            LIMIT :limit
        """
    )
    fun searchWithoutOrder(
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
            ORDER BY average_rating DESC NULLS LAST LIMIT :limit
        """
    )
    fun searchWithOrderByBest(
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
            ORDER BY |/(latitude^2-:centerLat^2 + longitude^2-:centerLong^2) ASC LIMIT :limit
        """
    )
    fun searchWithOrderByClosest(
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
    fun searchWithOrderByRecentlyActive(
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
    fun searchWithOrderByPopularity(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        limit: Int
    ): Flow<LoveSpot>
}
