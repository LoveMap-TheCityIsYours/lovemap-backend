package com.lovemap.lovemapbackend.love

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class LoveConverter(
    private val loverService: LoverService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun toDto(caller: Lover, love: Love): LoveResponse {
        val partnerName: String? = if (callerIsPartnerInLove(caller, love)) {
            caller.displayName
        } else {
            love.loverPartnerId?.let {
                loverService.unAuthorizedGetById(it).displayName
            }
        }
        return LoveResponse.of(love, partnerName)
    }

    suspend fun toDtoList(caller: Lover, loves: Flow<Love>): List<LoveResponse> {
        val start = System.currentTimeMillis()
        val allRelatedLovers: Map<Long, Lover> = getAllRelatedLovers(loves)
        val loveResponses = loves.map { love ->
            loveToResponse(caller, love, allRelatedLovers)
        }.toList()
        logger.info { "Converting Loves the optimized way took ${System.currentTimeMillis() - start} ms." }
        return loveResponses
    }

    private suspend fun getAllRelatedLovers(loves: Flow<Love>): Map<Long, Lover> {
        val lovers = loves.map { it.loverId }.toSet()
        val loverPartners = loves.toSet().mapNotNull { it.loverPartnerId }
        val allRelatedLoverIds: Set<Long> = lovers + loverPartners
        return loverService.getAllByIds(allRelatedLoverIds)
    }

    private fun loveToResponse(
        caller: Lover,
        love: Love,
        allRelatedLovers: Map<Long, Lover>
    ): LoveResponse {
        val partnerName: String? = if (callerIsPartnerInLove(caller, love)) {
            allRelatedLovers[love.loverId]?.displayName
        } else {
            love.loverPartnerId?.let {
                allRelatedLovers[it]?.displayName
            }
        }
        return LoveResponse.of(love, partnerName)
    }

    private fun callerIsPartnerInLove(
        caller: Lover,
        love: Love
    ): Boolean = caller.id == love.loverPartnerId


}