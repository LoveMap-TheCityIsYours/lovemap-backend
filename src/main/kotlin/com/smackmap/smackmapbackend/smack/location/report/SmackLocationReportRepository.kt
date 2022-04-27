package com.smackmap.smackmapbackend.smack.location.report

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface SmackLocationReportRepository : CoroutineSortingRepository<SmackLocationReport, Long> {
    suspend fun findBySmackerIdAndSmackLocationId(reviewerId: Long, smackLocationId: Long): SmackLocationReport?

    fun findAllBySmackLocationIdIn(locationIds: Collection<Long>): Flow<SmackLocationReport>

    fun findAllBySmackerId(smackerId: Long): Flow<SmackLocationReport>
}