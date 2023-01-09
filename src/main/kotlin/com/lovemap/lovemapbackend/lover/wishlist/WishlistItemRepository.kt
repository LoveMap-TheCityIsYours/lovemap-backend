package com.lovemap.lovemapbackend.lover.wishlist

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.sql.Timestamp

interface WishlistItemRepository : CoroutineCrudRepository<WishlistItem, Long> {
    fun findByLoverId(loverId: Long): Flow<WishlistItem>
    suspend fun findByLoverIdAndLoveSpotId(loverId: Long, loveSpotId: Long): WishlistItem?
    suspend fun existsByLoverIdAndLoveSpotId(loverId: Long, loveSpotId: Long): Boolean
    suspend fun deleteAllByLoveSpotId(loveSpotId: Long)

    @Query(
        """
            SELECT * FROM wishlist_element
            WHERE added_at > :addedAt
            ORDER BY added_at DESC
        """
    )
    fun findAllAfterAddedAt(addedAt: Timestamp): Flow<WishlistItem>

    fun findAllByLoveSpotId(loveSpotId: Long): Flow<WishlistItem>
}