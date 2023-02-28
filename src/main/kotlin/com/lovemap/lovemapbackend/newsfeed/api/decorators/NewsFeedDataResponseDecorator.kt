package com.lovemap.lovemapbackend.newsfeed.api.decorators

import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto

interface NewsFeedDataResponseDecorator {
    fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType
    fun decorate(initialized: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse
}
