package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto

interface NewsFeedDataResponseDecorator {
    fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType
    fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse
}
