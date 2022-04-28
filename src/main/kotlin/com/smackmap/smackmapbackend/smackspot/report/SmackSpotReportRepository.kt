package com.smackmap.smackmapbackend.smackspot.report

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface SmackSpotReportRepository : CoroutineSortingRepository<SmackSpotReport, Long> {
    suspend fun findBySmackerIdAndSmackSpotId(reviewerId: Long, smackSpotId: Long): SmackSpotReport?

    fun findAllBySmackSpotIdIn(locationIds: Collection<Long>): Flow<SmackSpotReport>

    fun findAllBySmackerId(smackerId: Long): Flow<SmackSpotReport>
}