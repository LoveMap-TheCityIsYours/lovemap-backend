package com.smackmap.smackmapbackend.smack.location

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface SmackLocationRepository : CoroutineSortingRepository<SmackLocation, Long> {

    @Query(
        "SELECT * FROM smack_location WHERE " +
                "longitude >= :longFrom AND longitude <= :longTo AND " +
                "latitude >= :latFrom AND latitude <= :latTo " +
                "ORDER BY average_rating DESC LIMIT :limit"
    )
    fun search(
        longFrom: Double,
        longTo: Double,
        latFrom: Double,
        latTo: Double,
        limit: Int
    ): Flow<SmackLocation>
}
