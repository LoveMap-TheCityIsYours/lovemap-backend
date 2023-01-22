package com.lovemap.lovemapbackend.newsfeed

import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.processor.NewsFeedProcessor
import com.lovemap.lovemapbackend.utils.ValidatorService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@Service
class NewsFeedService(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val validatorService: ValidatorService,
    private val newsFeedProcessor: NewsFeedProcessor
) {
    private val logger = KotlinLogging.logger {}
    private var cacheFilled = AtomicBoolean(false)
    private val feedCache = CopyOnWriteArrayList<NewsFeedItemDto>()
    private val processedFeedCache = CopyOnWriteArrayList<NewsFeedItemDto>()

    suspend fun reloadCache() {
        fillCacheFromDatabase()
    }

    suspend fun getWholeFeed(): List<NewsFeedItemResponse> {
        return if (!cacheFilled.getAndSet(true)) {
            fillCacheFromDatabase()
        } else {
            logger.info { "Returning processedFeedCache with size: ${processedFeedCache.size}" }
            ArrayList(processedFeedCache)
        }.map { newsFeedItemConverter.dtoToResponse(it) }
    }

    suspend fun getNewsFeedPage(page: Int, size: Int): List<NewsFeedItemResponse> {
        if (!cacheFilled.getAndSet(true)) {
            fillCacheFromDatabase()
        }
        validatorService.validatePageRequest(page, size, feedCache.size)
        val pageItems = feedCache.subList(page * size, min(page * size + size, feedCache.size))
        logger.info { "Returning unprocessed page with size: ${pageItems.size}" }
        return pageItems.map { newsFeedItemConverter.dtoToResponse(it) }
    }

    suspend fun getProcessedNewsFeedPage(page: Int, size: Int): List<NewsFeedItemResponse> {
        if (!cacheFilled.getAndSet(true)) {
            fillCacheFromDatabase()
        }
        validatorService.validatePageRequest(page, size, processedFeedCache.size)
        val pageItems = processedFeedCache.subList(page * size, min(page * size + size, processedFeedCache.size))
        logger.info { "Returning processed page with size: ${pageItems.size}" }
        return pageItems.map { newsFeedItemConverter.dtoToResponse(it) }
    }

    private suspend fun fillCacheFromDatabase(): List<NewsFeedItemDto> {
        logger.info { "Updating NewsFeed Cache" }

        val unprocessedFeed = newsFeedProcessor.getUnprocessedFeed()
        feedCache.clear()
        feedCache.addAll(unprocessedFeed)

        val processedFeed = newsFeedProcessor.getProcessedFeed()
        processedFeedCache.clear()
        processedFeedCache.addAll(processedFeed)

        return processedFeed
    }

    fun removeFromCache(type: NewsFeedItemDto.Type, referenceId: Long) {
        feedCache.removeIf { it.referenceId == referenceId && it.type == type }
        processedFeedCache.removeIf { it.referenceId == referenceId && it.type == type }
    }
}