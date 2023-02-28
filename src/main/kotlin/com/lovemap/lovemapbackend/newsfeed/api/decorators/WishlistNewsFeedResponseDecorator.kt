package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.api.WishlistNewsFeedResponse
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.WishlistNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class WishlistNewsFeedResponseDecorator : NewsFeedDataResponseDecorator {

    override fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType {
        return ProcessedNewsFeedItemDto.ProcessedType.WISHLIST_ITEM
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
