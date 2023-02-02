package com.lovemap.lovemapbackend.lover.relation

import com.lovemap.lovemapbackend.lover.LoverView
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp
import java.time.Instant

@Table
data class Relation(
    @Id
    var id: Long = 0,

    @Column("status")
    var status: Status,

    @Column("source_id")
    var sourceId: Long,

    @Column("target_id")
    var targetId: Long,

    @Column("created_at")
    var createdAt: Timestamp = Timestamp.from(Instant.now())
) {
    enum class Status {
        FOLLOWING, PARTNER, BLOCKED
    }
}

data class LoverRelation(
    val loverView: LoverView,
    val rank: Int,
    val relationStatus: Relation.Status
)

data class LoverRelations(
    val loverId: Long,
    val relations: Flow<LoverRelation>
)
