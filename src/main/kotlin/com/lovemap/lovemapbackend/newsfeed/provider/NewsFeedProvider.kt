package com.lovemap.lovemapbackend.newsfeed.provider

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface NewsFeedProvider {
    fun supportedType(): NewsFeedItem.Type
    suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto>
}