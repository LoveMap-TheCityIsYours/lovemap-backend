package com.smackmap.smackmapbackend.smack

import com.smackmap.smackmapbackend.smack.location.SmackLocationDto
import com.smackmap.smackmapbackend.smack.location.review.SmackLocationReviewDto
import kotlinx.coroutines.flow.Flow

data class SmackDto(
    val id: Long,
    val name: String,
    val smackLocationId: Long,
    val smackerId: Long,
    val smackerPartnerId: Long? = null,
) {
    companion object {
        fun of(smack: Smack): SmackDto {
            return SmackDto(
                id = smack.id,
                name = smack.name,
                smackLocationId = smack.smackLocationId,
                smackerId = smack.smackerId,
                smackerPartnerId = smack.smackerPartnerId
            )
        }
    }
}

data class SmackListDto(
    val smacks: Flow<SmackDto>,
    val smackLocations: Flow<SmackLocationDto>,
    val smackLocationReviews: Flow<SmackLocationReviewDto>
)

data class CreateSmackRequest(
    val name: String,
    val smackLocationId: Long,
    val smackerId: Long,
    val smackerPartnerId: Long? = null,
)
