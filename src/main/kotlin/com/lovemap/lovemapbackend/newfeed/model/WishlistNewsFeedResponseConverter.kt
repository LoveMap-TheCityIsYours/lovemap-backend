package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class WishlistNewsFeedResponseConverter : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.WISHLIST_ITEM
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is WishlistNewsFeedData) {
            initializedResponse.copy(
                wishlist = WishlistNewsFeedResponse(
                    id = newsFeedData.id,
                    loverId = newsFeedData.loverId,
                    loveSpotId = newsFeedData.loveSpotId,
                    addedAt = newsFeedData.addedAt
                )
            )
        } else {
            initializedResponse
        }
    }

}
