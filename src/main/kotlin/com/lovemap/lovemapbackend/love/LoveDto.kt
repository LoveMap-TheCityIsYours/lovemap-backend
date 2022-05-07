package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewDto

data class LoveDto(
    val id: Long,
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val loverPartnerId: Long? = null,
    val note: String? = null,
) {
    companion object {
        fun of(love: Love): LoveDto {
            return LoveDto(
                id = love.id,
                name = love.name,
                loveSpotId = love.loveSpotId,
                loverId = love.loverId,
                loverPartnerId = love.loverPartnerId,
                note = love.note
            )
        }
    }
}

data class LoveListDto(
    val loves: List<LoveDto>,
    val loveSpots: List<LoveSpotDto>,
    val loveSpotReviews: List<LoveSpotReviewDto>
)

data class CreateLoveRequest(
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val loverPartnerId: Long? = null,
    val note: String? = null,
)
