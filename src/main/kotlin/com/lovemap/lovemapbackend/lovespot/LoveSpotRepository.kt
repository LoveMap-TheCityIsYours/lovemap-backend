package com.lovemap.lovemapbackend.lovespot

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotRepository : CoroutineSortingRepository<LoveSpot, Long> {

    @Query(
        "SELECT * FROM love_location WHERE " +
                "longitude >= :longFrom AND longitude <= :longTo AND " +
                "latitude >= :latFrom AND latitude <= :latTo " +
                "ORDER BY average_rating DESC NULLS LAST LIMIT :limit"
    )
    fun search(
        longFrom: Double,
        longTo: Double,
        latFrom: Double,
        latTo: Double,
        limit: Int
    ): Flow<LoveSpot>
}