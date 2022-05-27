package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.relation.RelationService
import com.lovemap.lovemapbackend.security.AuthorizationService
import org.springframework.stereotype.Service

@Service
class LoverRelationService(
    private val authorizationService: AuthorizationService,
    private val loverService: LoverService,
    private val loverConverter: LoverConverter,
    private val relationService: RelationService,
) {

    suspend fun getWithRelations(id: Long): LoverRelationsDto {
        val lover = loverService.getById(id)
        return loverConverter.toRelationsDto(lover, relationService.getRelationsFrom(id))
    }

    suspend fun getWithRelations(lover: Lover): LoverRelationsDto {
        return loverConverter.toRelationsDto(lover, relationService.getRelationsFrom(lover.id))
    }

    suspend fun getByUuid(uuid: String): LoverViewDto {
        val caller = authorizationService.getCaller()
        val lover = loverService.getByUuid(uuid, caller)
        relationService.checkBlockingBetweenLovers(caller.id, lover.id)
        val relationStatusDto = relationService.getRelationStatusDto(caller.id, lover.id)
        return LoverViewDto.of(lover, relationStatusDto)
    }

    suspend fun getById(id: Long): LoverViewDto {
        val caller = authorizationService.getCaller()
        val lover = loverService.unAuthorizedGetById(id)
        relationService.checkBlockingBetweenLovers(caller.id, lover.id)
        val relationStatusDto = relationService.getRelationStatusDto(caller.id, lover.id)
        return LoverViewDto.of(lover, relationStatusDto)
    }
}