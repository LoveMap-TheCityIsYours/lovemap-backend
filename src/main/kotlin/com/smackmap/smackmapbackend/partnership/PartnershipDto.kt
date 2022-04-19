package com.smackmap.smackmapbackend.partnership

import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    val partnerships: Flow<PartnershipResponse>
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
    val initiatorId: Long,
    val respondentId: Long,
    val partnershipStatus: PartnershipApiStatus,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val initiateDate: Instant?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val respondDate: Instant?
) {
    companion object {
        fun of(partnership: Partnership): PartnershipResponse {
            return PartnershipResponse(
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