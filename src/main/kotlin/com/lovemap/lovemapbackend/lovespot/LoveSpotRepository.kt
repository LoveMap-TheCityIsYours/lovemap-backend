package com.lovemap.lovemapbackend.lovespot

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface LoveSpotRepository : CoroutineSortingRepository<LoveSpot, Long>, CoroutineCrudRepository<LoveSpot, Long> {

    @Query(
        """
            SELECT * FROM love_location 
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) 
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            AND type IN (:typeFilter)
            ORDER BY random() 
            LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByRandom(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) 
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            AND type IN (:typeFilter)
            ORDER BY 
                average_rating DESC NULLS LAST, 
                popularity DESC NULLS LAST 
            LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByRating(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) 
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            AND type IN (:typeFilter)
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
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) 
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            AND type IN (:typeFilter)
            ORDER BY last_active_at DESC NULLS LAST LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByRecentlyActive(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) 
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            AND type IN (:typeFilter)
            ORDER BY popularity DESC LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByPopularity(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo) 
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo) 
            AND type IN (:typeFilter)
            ORDER BY id DESC LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByNewest(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location
            WHERE longitude >= LEAST(:longFrom,:longTo) AND longitude <= GREATEST(:longFrom,:longTo)
            AND latitude >= LEAST(:latFrom,:latTo) AND latitude <= GREATEST(:latFrom,:latTo)
            AND type IN (:typeFilter) 
            AND last_photo_added_at IS NOT NULL
            AND number_of_photos > 0 
            ORDER BY last_photo_added_at DESC LIMIT :limit
        """
    )
    fun findByCoordinatesOrderByRecentPhoto(
        latFrom: Double,
        longFrom: Double,
        latTo: Double,
        longTo: Double,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.city = :city
            ) 
            AND type IN (:typeFilter)
            ORDER BY 
                average_rating DESC NULLS LAST, 
                popularity DESC NULLS LAST 
            LIMIT :limit
        """
    )
    fun findByCityOrderByRating(
        city: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.country = :country
            ) 
            AND type IN (:typeFilter)
            ORDER BY 
                average_rating DESC NULLS LAST, 
                popularity DESC NULLS LAST 
            LIMIT :limit
        """
    )
    fun findByCountryOrderByRating(
        country: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.city = :city
            ) 
            AND type IN (:typeFilter)
            ORDER BY |/((latitude-:centerLat)^2 + (longitude-:centerLong)^2) ASC LIMIT :limit
        """
    )
    fun findByCityOrderByClosest(
        centerLat: Double,
        centerLong: Double,
        city: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.country = :country
            ) 
            AND type IN (:typeFilter)
            ORDER BY |/((latitude-:centerLat)^2 + (longitude-:centerLong)^2) ASC LIMIT :limit
        """
    )
    fun findByCountryOrderByClosest(
        centerLat: Double,
        centerLong: Double,
        country: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.city = :city
            ) 
            AND type IN (:typeFilter)
            ORDER BY last_active_at DESC NULLS LAST LIMIT :limit
        """
    )
    fun findByCityOrderByRecentlyActive(
        city: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.country = :country
            ) 
            AND type IN (:typeFilter)
            ORDER BY last_active_at DESC NULLS LAST LIMIT :limit
        """
    )
    fun findByCountryOrderByRecentlyActive(
        country: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.city = :city
            ) 
            AND type IN (:typeFilter)
            ORDER BY popularity DESC LIMIT :limit
        """
    )
    fun findByCityOrderByPopularity(
        city: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.country = :country
            ) 
            AND type IN (:typeFilter)
            ORDER BY popularity DESC LIMIT :limit
        """
    )
    fun findByCountryOrderByPopularity(
        country: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location  
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.city = :city
            ) 
            AND type IN (:typeFilter)
            ORDER BY id DESC LIMIT :limit
        """
    )
    fun findByCityOrderByNewest(
        city: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.country = :country
            ) 
            AND type IN (:typeFilter)
            ORDER BY id DESC LIMIT :limit
        """
    )
    fun findByCountryOrderByNewest(
        country: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>


    @Query(
        """
            SELECT * FROM love_location  
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.city = :city
            ) 
            AND type IN (:typeFilter)
            AND last_photo_added_at IS NOT NULL
            AND number_of_photos > 0 
            ORDER BY last_photo_added_at DESC LIMIT :limit
        """
    )
    fun findByCityOrderByRecentPhoto(
        city: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location 
            WHERE geo_location_id IN 
            (
                SELECT id FROM geo_location 
                WHERE geo_location.country = :country
            ) 
            AND type IN (:typeFilter)
            AND last_photo_added_at IS NOT NULL
            AND number_of_photos > 0 
            ORDER BY last_photo_added_at DESC LIMIT :limit
        """
    )
    fun findByCountryOrderByRecentPhoto(
        country: String,
        typeFilter: Set<LoveSpot.Type>,
        limit: Int
    ): Flow<LoveSpot>

    @Query(
        """
            SELECT * FROM love_location
            WHERE created_at > :createdAt
            ORDER BY created_at DESC
        """
    )
    fun findAllAfterCreatedAt(createdAt: Timestamp): Flow<LoveSpot>
}
