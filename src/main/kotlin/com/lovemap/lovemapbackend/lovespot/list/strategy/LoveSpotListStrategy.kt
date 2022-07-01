package com.lovemap.lovemapbackend.lovespot.list.strategy

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import kotlinx.coroutines.flow.Flow

interface LoveSpotListStrategy {
    suspend fun listSpots(): Flow<LoveSpot>
}