package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewDto
import java.time.Instant

data class LoveDto(
    val id: Long,
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val loverPartnerId: Long? = null,
    val note: String? = null,
    val happenedAt: Instant? = null,
) {
    companion object {
        fun of(love: Love): LoveDto {
            return LoveDto(
                id = love.id,
                name = love.name,
                loveSpotId = love.loveSpotId,
                loverId = love.loverId,
                loverPartnerId = love.loverPartnerId,
                note = love.note,
                happenedAt = love.happenedAt.toInstant()
            )
        }
    }
}

data class CreateLoveRequest(
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val happenedAt: String? = null,
    val loverPartnerId: Long? = null,
    val note: String? = null,
)
