package com.lovemap.lovemapbackend.lover.wishlist

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import java.time.Instant

data class WishlistResponse(
    val wishlistElementId: Long,
    val loverId: Long,
    val addedAt: Instant,
    val loveSpot: LoveSpotResponse,
) {
    companion object {
        fun of(wishlistElement: WishlistElement, loveSpot: LoveSpot): WishlistResponse {
            return WishlistResponse(
                wishlistElementId = wishlistElement.id,
                loverId = wishlistElement.loverId,
                addedAt = wishlistElement.addedAt.toInstant(),
                loveSpot = LoveSpotResponse.of(loveSpot)
            )
        }
    }
}