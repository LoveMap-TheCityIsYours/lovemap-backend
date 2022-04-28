package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.smackspot.SmackSpotDto
import com.smackmap.smackmapbackend.smackspot.review.SmackSpotReviewDto

data class SmackDto(
    val id: Long,
    val name: String,
    val smackSpotId: Long,
    val smackerId: Long,
    val smackerPartnerId: Long? = null,
) {
    companion object {
        fun of(smack: Smack): SmackDto {
            return SmackDto(
                id = smack.id,
                name = smack.name,
                smackSpotId = smack.smackSpotId,
                smackerId = smack.smackerId,
                smackerPartnerId = smack.smackerPartnerId
            )
        }
    }
}

data class SmackListDto(
    val smacks: List<SmackDto>,
    val smackSpots: List<SmackSpotDto>,
    val smackSpotReviews: List<SmackSpotReviewDto>
)

data class CreateSmackRequest(
    val name: String,
    val smackSpotId: Long,
    val smackerId: Long,
    val smackerPartnerId: Long? = null,
)
