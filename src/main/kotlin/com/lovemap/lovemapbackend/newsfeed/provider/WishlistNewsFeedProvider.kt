package com.lovemap.lovemapbackend.newsfeed.provider

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lover.wishlist.WishlistItem
import com.lovemap.lovemapbackend.lover.wishlist.WishlistService
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.data.WishlistNewsFeedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WishlistNewsFeedProvider(
    private val wishlistService: WishlistService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val cachedLoverService: CachedLoverService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for Wishlists from $generateFrom" }
        val wishlistItems = wishlistService.getWishlistItemsFrom(generateFrom)
        return wishlistItems.map {
            NewsFeedItemDto(
                type = NewsFeedItemDto.Type.WISHLIST_ITEM,
                generatedAt = generationTime,
                referenceId = it.id,
                country = cachedLoveSpotService.getCountryByLoveSpotId(it.loveSpotId),
                publicLover = cachedLoverService.getIfProfileIsPublic(it.loverId),
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
