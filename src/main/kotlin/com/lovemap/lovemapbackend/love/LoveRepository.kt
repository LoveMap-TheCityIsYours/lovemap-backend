package com.lovemap.lovemapbackend.love

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.sql.Timestamp

interface LoveRepository : CoroutineCrudRepository<Love, Long> {
    fun findDistinctByLoverIdOrLoverPartnerIdOrderByHappenedAtDesc(loverId: Long, loverPartnerId: Long): Flow<Love>
    fun findByLoveSpotIdOrderByHappenedAtDesc(loveSpotId: Long): Flow<Love>

    suspend fun findFirstByLoveSpotIdOrderByHappenedAtDesc(loveSpotId: Long): Love?
    suspend fun deleteByLoveSpotId(loveSpotId: Long)
    suspend fun countByLoveSpotId(loveSpotId: Long): Long

    @Query(
        """
            SELECT * FROM love
            WHERE happened_at > :happenedAt
            ORDER BY happened_at DESC
        """
    )
    fun findAllAfterHappenedAt(happenedAt: Timestamp): Flow<Love>
}