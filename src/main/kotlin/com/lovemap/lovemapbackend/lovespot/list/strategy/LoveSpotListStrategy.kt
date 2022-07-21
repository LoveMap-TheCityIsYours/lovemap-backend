package com.lovemap.lovemapbackend.lovespot.list.strategy

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.list.ListLocationDto
import com.lovemap.lovemapbackend.lovespot.list.ListOrderingDto
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto
import kotlinx.coroutines.flow.Flow

interface LoveSpotListStrategy {
    fun getSupportedConditions(): Set<Pair<ListLocationDto, ListOrderingDto>>
    suspend fun listSpots(listDto: LoveSpotAdvancedListDto): Flow<LoveSpot>
}