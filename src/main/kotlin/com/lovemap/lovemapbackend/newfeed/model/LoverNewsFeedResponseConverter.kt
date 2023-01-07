package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import org.springframework.stereotype.Component

@Component
class LoverNewsFeedResponseConverter : TypeBasedNewsFeedResponseConverter<LoverNewsFeedResponse> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVER
    }

    override fun convert(dto: NewsFeedItemDto): LoverNewsFeedResponse? {
        val newsFeedData = dto.newsFeedData
        return if (newsFeedData is LoverNewsFeedData) {
            LoverNewsFeedResponse(
                id = newsFeedData.id,
                userName = newsFeedData.userName,
                publicProfile = newsFeedData.publicProfile,
                joinedAt = newsFeedData.joinedAt,
                rank = newsFeedData.rank,
                points = newsFeedData.points,
                uuid = newsFeedData.uuid
            )
        } else {
            null
        }
    }

}
