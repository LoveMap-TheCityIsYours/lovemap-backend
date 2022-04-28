package com.smackmap.smackmapbackend.relation

import com.smackmap.smackmapbackend.smacker.SmackerView
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Relation(
    @Id
    var id: Long = 0,

    @Column("status")
    var status: Status,

    @Column("source_id")
    var sourceId: Long,

    @Column("target_id")
    var targetId: Long,
) {
    enum class Status {
        FOLLOWING, PARTNER, BLOCKED
    }
}

data class SmackerRelation(
    val smackerView: SmackerView,
    val rank: Int,
    val relationStatus: Relation.Status
)

data class SmackerRelations(
    val smackerId: Long,
    val relations: Flow<SmackerRelation>
)
