package com.smackmap.smackmapbackend.partnership

import java.time.Instant

data class RequestPartnershipRequest(
    val requestorId: Long,
    val requesteeId: Long,
)

data class RespondPartnershipRequest(
    val requestorId: Long,
    val requesteeId: Long,
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
                partnerships = smackerPartnerShips.partnerships.map { PartnershipResponse.of(it) }
            )
        }
    }
}

data class PartnershipResponse(
    val id: Long,
    val requestorId: Long,
    val requesteeId: Long,
    val partnershipStatus: PartnershipApiStatus,
    val startDate: Instant?,
    val endDate: Instant?
) {
    companion object {
        fun of(partnership: Partnership): PartnershipResponse {
            return PartnershipResponse(
                id = partnership.id,
                requestorId = partnership.requestorId,
                requesteeId = partnership.requesteeId,
                partnershipStatus = PartnershipApiStatus.of(partnership.partnershipStatus),
                startDate = partnership.startDate?.toInstant(),
                endDate = partnership.endDate?.toInstant()
            )
        }
    }
}

enum class PartnershipApiStatus {
    REQUESTED, LIVE, ENDED;

    companion object {
        fun of(partnershipStatus: PartnershipStatus): PartnershipApiStatus {
            return when (partnershipStatus) {
                PartnershipStatus.REQUESTED -> REQUESTED
                PartnershipStatus.LIVE -> LIVE
                PartnershipStatus.ENDED -> ENDED
            }
        }
    }
}