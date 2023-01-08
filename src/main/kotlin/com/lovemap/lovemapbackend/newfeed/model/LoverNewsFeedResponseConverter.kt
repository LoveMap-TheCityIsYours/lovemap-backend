package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoverNewsFeedResponseConverter : TypeBasedNewsFeedResponseDecorator {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVER
    }

    override fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse {
        return if (newsFeedData is LoverNewsFeedData) {
            initializedResponse.copy(
                lover = LoverNewsFeedResponse(
                    id = newsFeedData.id,
                    userName = newsFeedData.userName,
                    publicProfile = newsFeedData.publicProfile,
                    joinedAt = newsFeedData.joinedAt,
                    rank = newsFeedData.rank,
                    points = newsFeedData.points,
                    uuid = newsFeedData.uuid
                )
            )
        } else {
            initializedResponse
        }
    }

}
