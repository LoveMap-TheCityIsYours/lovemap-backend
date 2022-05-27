package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.relation.LoverRelations
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class LoverConverter(
    @Value("\${lovemap.admins.emails}")
    private val adminEmails: List<String>,
) {

    fun toDto(lover: Lover): LoverDto {
        return LoverDto.of(lover, adminEmails.contains(lover.email))
    }

    suspend fun toRelationsDto(lover: Lover, loverRelations: LoverRelations): LoverRelationsDto {
        return LoverRelationsDto.of(lover, loverRelations, adminEmails.contains(lover.email))
    }
}