package com.lovemap.lovemapbackend.newfeed.model.response.decorators

import com.lovemap.lovemapbackend.newfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.response.NewsFeedItemResponse

interface TypeBasedNewsFeedResponseDecorator {
    fun supportedType(): NewsFeedItemDto.Type
    fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse
}
