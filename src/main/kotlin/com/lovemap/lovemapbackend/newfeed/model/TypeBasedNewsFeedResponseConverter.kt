package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem

interface TypeBasedNewsFeedResponseConverter<T> {
    fun supportedType(): NewsFeedItem.Type
    fun convert(dto: NewsFeedItemDto): T?
}
