package com.lovemap.lovemapbackend.lover

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface LoverRepository : CoroutineCrudRepository<Lover, Long>, CoroutineSortingRepository<Lover, Long> {
    suspend fun findByUserName(userName: String): Lover?
    suspend fun findByEmail(email: String): Lover?
    suspend fun findByUuid(uuid: String): Lover?
    suspend fun existsByEmail(email: String): Boolean

    @Query(
        """
            SELECT * FROM lover
            WHERE created_at > :createdAt
            ORDER BY created_at DESC
        """
    )
    fun findAllAfterCreatedAt(createdAt: Timestamp): Flow<Lover>

    fun findAllByIdInAndPublicProfile(ids: Collection<Long>, publicProfile: Boolean): Flow<Lover>

    @Query(
        """
            SELECT * FROM lover 
            ORDER BY points DESC 
            LIMIT :limit
        """
    )
    fun findTopLimitOrderByPoints(limit: Int): Flow<Lover>

    @Modifying
    @Query(
        """
            UPDATE lover 
            SET hall_of_fame_position = NULL
            WHERE hall_of_fame_position IS NOT NULL
        """
    )
    suspend fun nullAllHofPositions(): Int
}