package com.lovemap.lovemapbackend.newsfeed

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
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
        try {
            newsFeedRepository.deleteByTypeAndReferenceId(type, referenceId)
            newsFeedService.removeFromCache(NewsFeedItemDto.Type.of(type), referenceId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete NewsFeedItem with type '$type' and referenceId '$referenceId'" }
        }
    }
}
