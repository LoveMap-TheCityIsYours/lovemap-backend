package com.lovemap.lovemapbackend.lover.wishlist

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WishlistElementRepository : CoroutineCrudRepository<WishlistElement, Long> {
    fun findByLoverId(loverId: Long): Flow<WishlistElement>
    suspend fun findByLoverIdAndLoveSpotId(loverId: Long, loveSpotId: Long): WishlistElement?
}