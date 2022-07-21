package com.lovemap.lovemapbackend.partnership

import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

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

data class LoverPartnershipsResponse(
    val loverId: Long,
    val partnerships: List<PartnershipResponse>
) {
    companion object {
        suspend fun of(loverPartnerShips: LoverPartnerships): LoverPartnershipsResponse {
            return LoverPartnershipsResponse(
                loverId = loverPartnerShips.loverId,
                partnerships = loverPartnerShips.relations.map { PartnershipResponse.of(it) }.toList()
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