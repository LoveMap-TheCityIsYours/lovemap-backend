package com.lovemap.lovemapbackend.lovespot.list.strategy

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.list.ListLocationType
import com.lovemap.lovemapbackend.lovespot.list.ListOrdering
import com.lovemap.lovemapbackend.lovespot.list.LoveSpotAdvancedListDto

interface LoveSpotListStrategy {
    fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>>
    suspend fun listSpots(listDto: LoveSpotAdvancedListDto): List<LoveSpot>
}