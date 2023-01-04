package com.lovemap.lovemapbackend.lover.wishlist

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import java.time.Instant

data class WishlistResponse(
    val wishlistItemId: Long,
    val loverId: Long,
    val addedAt: Instant,
    val loveSpot: LoveSpotResponse,
) {
    companion object {
        fun of(wishlistItem: WishlistItem, loveSpot: LoveSpot): WishlistResponse {
            return WishlistResponse(
                wishlistItemId = wishlistItem.id,
                loverId = wishlistItem.loverId,
                addedAt = wishlistItem.addedAt.toInstant(),
                loveSpot = LoveSpotResponse.of(loveSpot)
            )
        }
    }
}