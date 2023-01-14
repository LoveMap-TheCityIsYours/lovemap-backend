package com.lovemap.lovemapbackend.relation

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RelationRepository : CoroutineCrudRepository<Relation, Long> {
    fun findBySourceIdAndStatusIn(
        sourceId: Long,
        statusFilter: Set<Relation.Status>
    ): Flow<Relation>

    fun findByTargetIdAndStatusIn(
        targetId: Long,
        statusFilter: Set<Relation.Status>
    ): Flow<Relation>

    suspend fun existsBySourceIdAndTargetIdAndStatusIn(
        sourceId: Long,
        targetId: Long,
        statusFilter: Set<Relation.Status>
    ): Boolean

    suspend fun findBySourceIdAndTargetId(
        sourceId: Long,
        targetId: Long
    ): Relation?


    suspend fun deleteBySourceIdAndTargetId(
        sourceId: Long,
        targetId: Long
    )

    suspend fun existsBySourceIdAndTargetIdAndStatus(
        sourceId: Long,
        targetId: Long,
        relationStatus: Relation.Status
    ): Boolean
}