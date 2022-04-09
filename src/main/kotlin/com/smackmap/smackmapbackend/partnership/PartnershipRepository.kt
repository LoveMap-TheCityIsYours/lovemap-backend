package com.smackmap.smackmapbackend.partnership

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PartnershipRepository : CoroutineCrudRepository<Partnership, Long> {
    fun findByRequestorIdAndPartnershipStatusIn(
        requestorId: Long,
        statusFilter: Set<PartnershipStatus>
    ): Flow<Partnership>

    fun findByRequesteeIdAndPartnershipStatusIn(
        requesteeId: Long,
        statusFilter: Set<PartnershipStatus>
    ): Flow<Partnership>
}