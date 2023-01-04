package com.lovemap.lovemapbackend.lover.wishlist

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovers/{loverId}/wishlist")
class WishlistController(
    private val wishlistService: WishlistService
) {

    @GetMapping
    suspend fun getWishlist(@PathVariable loverId: Long): List<WishlistResponse> {
        return wishlistService.getWishList(loverId)
    }

    @DeleteMapping("{wishlistItemId}")
    suspend fun deleteWishlistItem(@PathVariable loverId: Long, @PathVariable wishlistItemId: Long): List<WishlistResponse> {
        return wishlistService.deleteWishlistItem(loverId, wishlistItemId)
    }

    @PostMapping("/addSpot/{loveSpotId}")
    suspend fun addToWishlist(@PathVariable loverId: Long, @PathVariable loveSpotId: Long): List<WishlistResponse> {
        return wishlistService.addToWishlist(loverId, loveSpotId)
    }
}
