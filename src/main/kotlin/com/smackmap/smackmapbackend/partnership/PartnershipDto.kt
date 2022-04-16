package com.smackmap.smackmapbackend.partnership

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
        fun of(smackerPartnerShips: SmackerPartnerships): SmackerPartnershipsResponse {
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
    val initiateDate: Instant?,
    val respondDate: Instant?
) {
    companion object {
        fun of(partnership: Partnership): PartnershipResponse {
            return PartnershipResponse(
                id = partnership.id,
                initiatorId = partnership.initiatorId,
                respondentId = partnership.respondentId,
                partnershipStatus = PartnershipApiStatus.of(partnership.status),
                initiateDate = partnership.initiateDate?.toInstant(),
                respondDate = partnership.respondDate?.toInstant()
            )
        }
    }
}

enum class PartnershipApiStatus {
    PARTNERSHIP_REQUESTED, IN_PARTNERSHIP;

    companion object {
        fun of(partnershipStatus: Partnership.Status): PartnershipApiStatus {
            return when (partnershipStatus) {
                Partnership.Status.PARTNERSHIP_REQUESTED -> PARTNERSHIP_REQUESTED
                Partnership.Status.PARTNER -> IN_PARTNERSHIP
            }
        }
    }
}