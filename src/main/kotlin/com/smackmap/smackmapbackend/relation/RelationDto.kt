package com.smackmap.smackmapbackend.relation

enum class RelationApiStatus {
    PARTNER, FOLLOWING, BLOCKED;

    companion object {
        fun of(relationStatus: Relation.Status): RelationApiStatus {
            return when (relationStatus) {
                Relation.Status.FOLLOWING -> FOLLOWING
                Relation.Status.PARTNER -> PARTNER
                Relation.Status.BLOCKED -> BLOCKED
            }
        }
    }
}