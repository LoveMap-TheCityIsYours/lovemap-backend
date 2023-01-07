package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lover.wishlist.WishlistItem
import com.lovemap.lovemapbackend.lover.wishlist.WishlistService
import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoveNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.WishlistNewsFeedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WishlistNewsFeedProvider(
    private val wishlistService: WishlistService
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger{}

    override fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for Wishlists from $generateFrom" }
        val wishlistItems = wishlistService.getWishlistItemsFrom(generateFrom)
        return wishlistItems.map {
            NewsFeedItemDto(
                type = NewsFeedItem.Type.WISHLIST_ITEM,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = wishlistToNewsFeedData(it)
            )
        }
    }

    private suspend fun wishlistToNewsFeedData(wishlistItem: WishlistItem): WishlistNewsFeedData {
        return WishlistNewsFeedData(
            id = wishlistItem.id,
            loveSpotId = wishlistItem.loveSpotId,
            loverId = wishlistItem.loverId,
            addedAt = wishlistItem.addedAt.toInstant()
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.WISHLIST_ITEM
    }
}
