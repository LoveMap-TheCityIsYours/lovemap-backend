package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class WishlistNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<WishlistNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.WISHLIST_ITEM
    }

    override fun convert(dto: NewsFeedItemDto): WishlistNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is WishlistNewsFeedData) {
            WishlistNewsFeedResponse(
                id = newsFeedData.id,
                loverId = newsFeedData.loverId,
                loveSpotId = newsFeedData.loveSpotId,
                addedAt = newsFeedData.addedAt
            )
        } else {
            null
        }
    }

}
