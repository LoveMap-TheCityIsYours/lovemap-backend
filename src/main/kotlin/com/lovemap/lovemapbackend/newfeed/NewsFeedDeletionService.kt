package com.lovemap.lovemapbackend.newfeed

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import org.springframework.stereotype.Service

@Service
class NewsFeedDeletionService(
    private val newsFeedService: NewsFeedService,
    private val newsFeedRepository: NewsFeedRepository
) {
    suspend fun deleteByTypeAndReferenceId(type: NewsFeedItem.Type, referenceId: Long) {
        newsFeedRepository.deleteByTypeAndReferenceId(type, referenceId)
        newsFeedService.removeFromCache(type, referenceId)
    }
}