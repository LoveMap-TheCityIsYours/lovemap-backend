package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface NewsFeedProvider {
    fun supportedType(): NewsFeedItem.Type
    suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto>
}