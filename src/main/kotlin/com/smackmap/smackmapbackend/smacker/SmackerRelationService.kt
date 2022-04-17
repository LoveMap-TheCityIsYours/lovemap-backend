package com.smackmap.smackmapbackend.smacker

import com.smackmap.smackmapbackend.relation.RelationService
import com.smackmap.smackmapbackend.security.SmackerAuthorizationService
import org.springframework.stereotype.Service

@Service
class SmackerRelationService(
    private val authorizationService: SmackerAuthorizationService,
    private val smackerService: SmackerService,
    private val relationService: RelationService
) {

    suspend fun getDtoById(id: Long): SmackerRelationsDto {
        val smacker = smackerService.getById(id)
        return SmackerRelationsDto.of(smacker, relationService.getRelationsFrom(id))
    }

    suspend fun getByLink(link: String): SmackerViewDto {
        val caller = authorizationService.getCaller()
        val smacker = smackerService.getByLink(link, caller)
        relationService.checkBlockingBetweenSmackers(caller.id, smacker.id)
        val relationStatusDto = relationService.getRelationStatusDto(caller.id, smacker.id)
        return SmackerViewDto.of(smacker, relationStatusDto)
    }

    suspend fun getWithRelations(smacker: Smacker): SmackerRelationsDto {
        return SmackerRelationsDto.of(smacker, relationService.getRelationsFrom(smacker.id))
    }
}