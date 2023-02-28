package com.lovemap.lovemapbackend.newsfeed.model.response.decorators

import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse

interface NewsFeedDataResponseDecorator {
    fun supportedType(): NewsFeedItemDto.Type
    fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: NewsFeedData): NewsFeedItemResponse
}

interface ProcessedNewsFeedDataResponseDecorator {
    fun supportedType(): ProcessedNewsFeedItemDto.ProcessedType
    fun decorate(initializedResponse: NewsFeedItemResponse, newsFeedData: ProcessedNewsFeedData): NewsFeedItemResponse
}
