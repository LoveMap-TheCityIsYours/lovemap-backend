package com.lovemap.lovemapbackend.lovespot.report

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotReportRepository : CoroutineSortingRepository<LoveSpotReport, Long>,
    CoroutineCrudRepository<LoveSpotReport, Long> {
    suspend fun findByLoverIdAndLoveSpotId(reviewerId: Long, loveSpotId: Long): LoveSpotReport?

    fun findAllByLoveSpotIdIn(locationIds: Collection<Long>): Flow<LoveSpotReport>

    fun findAllByLoverId(loverId: Long): Flow<LoveSpotReport>

    suspend fun deleteByLoveSpotId(loveSpotId: Long)
}