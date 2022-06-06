package com.lovemap.lovemapbackend.love

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface LoveRepository : CoroutineCrudRepository<Love, Long> {
    fun findDistinctByLoverIdOrLoverPartnerId(loverId: Long, loverPartnerId: Long): Flow<Love>
    fun findByLoveSpotId(loveSpotId: Long): Flow<Love>

    suspend fun deleteByLoveSpotId(loveSpotId: Long)
}