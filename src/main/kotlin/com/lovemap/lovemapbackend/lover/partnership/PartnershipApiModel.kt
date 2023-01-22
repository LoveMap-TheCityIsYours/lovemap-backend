package com.lovemap.lovemapbackend.lover.partnership

import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString

data class RequestPartnershipRequest(
    val initiatorId: Long,
    val respondentId: Long,
)


data class CancelPartnershipRequest(
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

data class LoverPartnershipsResponse(
    val loverId: Long,
    val partnerships: List<PartnershipResponse>
) {
    companion object {
        fun of(loverPartnerShips: LoverPartnership): LoverPartnershipsResponse {
            return LoverPartnershipsResponse(
                loverId = loverPartnerShips.loverId,
                partnerships = loverPartnerShips.partnership
                    ?.let { listOf(PartnershipResponse.of(it)) } ?: emptyList()
            )
        }
    }
}

data class PartnershipResponse(
    val id: Long,
    val initiatorId: Long,
    val respondentId: Long,
    val partnershipStatus: PartnershipApiStatus,
    val initiateDate: String?,
    val respondDate: String?
) {
    companion object {
        fun of(partnership: Partnership): PartnershipResponse {
            return PartnershipResponse(
                id = partnership.id,
                initiatorId = partnership.initiatorId,
                respondentId = partnership.respondentId,
                partnershipStatus = PartnershipApiStatus.of(partnership.status),
                initiateDate = partnership.initiateDate?.toInstant()?.toApiString(),
                respondDate = partnership.respondDate?.toInstant()?.toApiString()
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