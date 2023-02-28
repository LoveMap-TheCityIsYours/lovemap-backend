package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.WishlistNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.WishlistNewsFeedResponse
import org.springframework.stereotype.Component

@Component
class WishlistNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): NewsFeedItemDto.Type {
        return NewsFeedItemDto.Type.WISHLIST_ITEM
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
