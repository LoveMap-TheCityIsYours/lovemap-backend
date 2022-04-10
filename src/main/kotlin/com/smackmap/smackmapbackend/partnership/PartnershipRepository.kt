package com.smackmap.smackmapbackend.partnership

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PartnershipRepository : CoroutineCrudRepository<Partnership, Long> {
    suspend fun findByInitiatorIdAndRespondentId(initiatorId: Long, respondentId: Long): Partnership?
    fun findByInitiatorId(smackerId: Long): Flow<Partnership>
    fun findByRespondentId(smackerId: Long): Flow<Partnership>
}