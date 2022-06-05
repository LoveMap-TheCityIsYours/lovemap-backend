package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverService
import org.springframework.stereotype.Service

@Service
class LoveConverter(
    private val loverService: LoverService,
) {

    suspend fun toDto(caller: Lover, love: Love): LoveDto {
        val partnerName: String? = if (caller.id == love.loverPartnerId) {
            loverService.unAuthorizedGetById(love.loverId).userName
        } else {
            love.loverPartnerId?.let {
                loverService.unAuthorizedGetById(it).userName
            }
        }
        return LoveDto.of(love, partnerName)
    }
}