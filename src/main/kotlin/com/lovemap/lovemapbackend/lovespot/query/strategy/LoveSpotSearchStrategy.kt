package com.lovemap.lovemapbackend.lovespot.query.strategy

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.query.ListLocationType
import com.lovemap.lovemapbackend.lovespot.query.ListOrdering
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotSearchDto

interface LoveSpotSearchStrategy {
    fun getSupportedConditions(): Set<Pair<ListLocationType, ListOrdering>>
    suspend fun listSpots(listDto: LoveSpotSearchDto): List<LoveSpot>
}