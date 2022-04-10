package com.smackmap.smackmapbackend.relation

enum class RelationApiStatus {
    PARTNERSHIP_REQUESTED, IN_PARTNERSHIP, FOLLOWING, BLOCKED;

    companion object {
        fun of(relationStatus: RelationStatus): RelationApiStatus {
            return when (relationStatus) {
                RelationStatus.PARTNERSHIP_REQUESTED -> PARTNERSHIP_REQUESTED
                RelationStatus.PARTNER -> IN_PARTNERSHIP
                RelationStatus.FOLLOWING -> FOLLOWING
                RelationStatus.BLOCKED -> BLOCKED
            }
        }
    }
}