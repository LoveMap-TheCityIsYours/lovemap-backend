package com.lovemap.lovemapbackend.lover.wishlist

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WishlistItemRepository : CoroutineCrudRepository<WishlistItem, Long> {
    fun findByLoverId(loverId: Long): Flow<WishlistItem>
    suspend fun findByLoverIdAndLoveSpotId(loverId: Long, loveSpotId: Long): WishlistItem?
    suspend fun existsByLoverIdAndLoveSpotId(loverId: Long, loveSpotId: Long): Boolean
    suspend fun deleteAllByLoveSpotId(loveSpotId: Long)
}