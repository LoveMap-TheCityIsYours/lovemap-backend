package com.smackmap.smackmapbackend.relation

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RelationRepository : CoroutineCrudRepository<Relation, Long> {
    fun findBySourceIdAndRelationStatusIn(
        sourceId: Long,
        statusFilter: Set<RelationStatus>
    ): Flow<Relation>

    fun findByTargetIdAndRelationStatusIn(
        targetId: Long,
        statusFilter: Set<RelationStatus>
    ): Flow<Relation>

    suspend fun existsBySourceIdAndTargetIdAndRelationStatusIn(
        sourceId: Long,
        targetId: Long,
        statusFilter: Set<RelationStatus>
    ): Boolean

    suspend fun findBySourceIdAndTargetId(
        sourceId: Long,
        targetId: Long
    ): Relation?

    suspend fun findBySourceIdAndTargetIdAndRelationStatus(
        sourceId: Long,
        targetId: Long,
        relationStatus: RelationStatus
    ): Relation?
}