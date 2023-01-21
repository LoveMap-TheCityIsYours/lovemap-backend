package com.lovemap.lovemapbackend.newfeed

import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newfeed.processor.NewsFeedProcessor
import com.lovemap.lovemapbackend.utils.ValidatorService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

@Service
class NewsFeedService(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val validatorService: ValidatorService,
    private val newsFeedProcessor: NewsFeedProcessor
) {
    private val logger = KotlinLogging.logger {}
    private val cache = CopyOnWriteArrayList<NewsFeedItemDto>()

    suspend fun reloadCache() {
        fillCacheFromDatabase()
    }

    suspend fun getWholeFeed(): List<NewsFeedItemResponse> {
        return if (cache.isEmpty()) {
            fillCacheFromDatabase()
        } else {
            logger.info { "Returning cache with size: ${cache.size}" }
            ArrayList(cache)
        }.map { newsFeedItemConverter.dtoToResponse(it) }
    }

    suspend fun getNewsFeedPage(page: Int, size: Int): List<NewsFeedItemResponse> {
        if (cache.isEmpty()) {
            fillCacheFromDatabase()
        }
        validatorService.validatePageRequest(page, size, cache.size)
        val pageItems = cache.subList(page * size, min(page * size + size, cache.size))
        logger.info { "Returning page with size: ${pageItems.size}" }
        return pageItems.map { newsFeedItemConverter.dtoToResponse(it) }
    }

    private suspend fun fillCacheFromDatabase(): List<NewsFeedItemDto> {
        logger.info { "Updating Cache" }
        val processedFeed = newsFeedProcessor.getProcessedFeed()
        cache.clear()
        cache.addAll(processedFeed)
        return processedFeed
    }

    fun removeFromCache(type: NewsFeedItemDto.Type, referenceId: Long) {
        cache.removeIf { it.referenceId == referenceId && it.type == type }
    }
}