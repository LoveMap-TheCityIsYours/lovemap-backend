package com.lovemap.lovemapbackend.lover.partnership

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PartnershipRepository : CoroutineCrudRepository<Partnership, Long> {
    suspend fun findByInitiatorIdAndRespondentId(initiatorId: Long, respondentId: Long): Partnership?
    fun findByInitiatorId(loverId: Long): Flow<Partnership>
    fun findByRespondentId(loverId: Long): Flow<Partnership>
}