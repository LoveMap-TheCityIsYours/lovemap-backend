package com.smackmap.smackmapbackend.partnership

import com.smackmap.smackmapbackend.relation.Relation
import com.smackmap.smackmapbackend.relation.RelationStatus
import com.smackmap.smackmapbackend.relation.SmackerRelations
import java.lang.IllegalArgumentException
import java.time.Instant

data class RequestPartnershipRequest(
    val initiatorId: Long,
    val respondentId: Long,
)

data class RespondPartnershipRequest(
    val initiatorId: Long,
    val respondentId: Long,
    val response: PartnershipReaction
)

enum class PartnershipReaction {
    ACCEPT, DENY
}

data class SmackerPartnershipsResponse(
    val smackerId: Long,
    val partnerships: List<PartnershipResponse>
) {
    companion object {
        fun of(smackerPartnerShips: SmackerRelations): SmackerPartnershipsResponse {
            return SmackerPartnershipsResponse(
                smackerId = smackerPartnerShips.smackerId,
                partnerships = smackerPartnerShips.relations.map { PartnershipResponse.of(it) }
            )
        }
    }
}

data class PartnershipResponse(
    val id: Long,
    val initiatorId: Long,
    val respondentId: Long,
    val partnershipStatus: PartnershipApiStatus,
    val partnershipStartDate: Instant?,
    val partnershipEndDate: Instant?
) {
    companion object {
        fun of(relation: Relation): PartnershipResponse {
            return PartnershipResponse(
                id = relation.id,
                initiatorId = relation.sourceId,
                respondentId = relation.targetId,
                partnershipStatus = PartnershipApiStatus.of(relation.relationStatus),
                partnershipStartDate = relation.partnershipRespondDate?.toInstant(),
                partnershipEndDate = relation.partnershipEndDate?.toInstant()
            )
        }
    }
}

enum class PartnershipApiStatus {
    PARTNERSHIP_REQUESTED, IN_PARTNERSHIP;

    companion object {
        fun of(relationStatus: RelationStatus): PartnershipApiStatus {
            return when (relationStatus) {
                RelationStatus.PARTNERSHIP_REQUESTED -> PARTNERSHIP_REQUESTED
                RelationStatus.PARTNER -> IN_PARTNERSHIP
                else -> throw IllegalArgumentException("Cannot convert '$relationStatus' to PartnershipApiStatus.")
            }
        }
    }
}