package com.lovemap.lovemapbackend.newfeed

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class NewsFeedDeletionService(
    private val newsFeedService: NewsFeedService,
    private val newsFeedRepository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}
    suspend fun deleteByTypeAndReferenceId(type: NewsFeedItem.Type, referenceId: Long) {
        logger.info { "Deleting [$type] Type with [$referenceId] referenceId." }
        newsFeedRepository.deleteByTypeAndReferenceId(type, referenceId)
        newsFeedService.removeFromCache(type, referenceId)
    }
}