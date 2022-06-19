package com.lovemap.lovemapbackend.lovespot

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotRepository : CoroutineSortingRepository<LoveSpot, Long> {

    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= :longFrom AND longitude <= :longTo AND 
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
            longitude >= :longFrom AND longitude <= :longTo AND 
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
            longitude >= :longFrom AND longitude <= :longTo AND 
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

    // TODO: instead add columns: number_of_loves, last_love_at, number_of_comments, last_comment_at
    @Query(
        """
            SELECT * FROM love_location WHERE 
            longitude >= :longFrom AND longitude <= :longTo AND 
            latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            ORDER BY (
                SELECT MIN(love.happened_at) FROM love 
                WHERE love_location.id = love.love_location_id 
            ) DESC NULLS LAST 
            LIMIT :limit
        """
    )
    fun searchWithOrderByLastMadeLove(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        limit: Int
    ): Flow<LoveSpot>
}
