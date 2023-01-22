package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse

interface TypeBasedNewsFeedResponseDecorator {
    fun supportedType(): NewsFeedItemDto.Type
    fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse
}
