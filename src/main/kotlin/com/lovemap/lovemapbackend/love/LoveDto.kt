package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString

data class LoveDto(
    val id: Long,
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val loverPartnerId: Long? = null,
    val partnerName: String? = null,
    val note: String? = null,
    val happenedAt: String? = null,
) {
    companion object {
        fun of(love: Love, partnerName: String?): LoveDto {
            return LoveDto(
                id = love.id,
                name = love.name,
                loveSpotId = love.loveSpotId,
                loverId = love.loverId,
                loverPartnerId = love.loverPartnerId,
                partnerName = partnerName,
                note = love.note,
                happenedAt = love.happenedAt.toInstant().toApiString()
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

data class UpdateLoveRequest(
    val name: String? = null,
    val happenedAt: String? = null,
    val loverPartnerId: Long? = null,
    val note: String? = null,
)
