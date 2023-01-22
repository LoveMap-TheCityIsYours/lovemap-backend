package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.lover.relation.LoverRelations
import com.lovemap.lovemapbackend.lover.relation.LoverRelationsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class LoverConverter(
    @Value("\${lovemap.admins.emails}")
    private val adminEmails: List<String>,
) {

    fun toResponse(lover: Lover): LoverResponse {
        return LoverResponse.of(lover, adminEmails.contains(lover.email))
    }

    suspend fun toRelationsResponse(lover: Lover, loverRelations: LoverRelations): LoverRelationsResponse {
        return LoverRelationsResponse.of(lover, loverRelations, adminEmails.contains(lover.email))
    }
}
