package com.smackmap.smackmapbackend.relation


data class RelationDto(
    var sourceId: Long,
    var targetId: Long,
    var status: RelationStatusDto,
) {
    companion object {
        fun of(relation: Relation): RelationDto {
            return RelationDto(
                sourceId = relation.sourceId,
                targetId = relation.targetId,
                status = RelationStatusDto.of(relation.status)
            )
        }
    }
}

enum class RelationStatusDto {
    PARTNER, FOLLOWING, BLOCKED, NOTHING;

    companion object {
        fun of(relationStatus: Relation.Status?): RelationStatusDto {
            return when (relationStatus) {
                Relation.Status.FOLLOWING -> FOLLOWING
                Relation.Status.PARTNER -> PARTNER
                Relation.Status.BLOCKED -> BLOCKED
                null -> NOTHING
            }
        }
    }
}