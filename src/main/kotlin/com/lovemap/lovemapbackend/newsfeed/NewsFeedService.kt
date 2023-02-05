package com.lovemap.lovemapbackend.newsfeed

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.processor.NewsFeedProcessor
import com.lovemap.lovemapbackend.utils.ValidatorService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

@Service
class NewsFeedService(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val validatorService: ValidatorService,
    private val newsFeedProcessor: NewsFeedProcessor,
    private val newsFeedRepository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}
    private var cacheFilled = AtomicBoolean(false)
    private val processedFeedCache = CopyOnWriteArrayList<NewsFeedItemDto>()

    suspend fun reloadCache() {
        fillCacheFromDatabase()
    }

    suspend fun getActivitiesOfLover(loverId: Long): List<NewsFeedItemResponse> {
        val start = System.currentTimeMillis()
        val result = newsFeedRepository.findLastLimitOfLover(100, loverId)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .map { newsFeedItemConverter.dtoToResponse(it) }
            .toList()
        logger.info { "Returned NewsFeedItems for lover '$loverId' in ${System.currentTimeMillis() - start} ms" }
        return result
    }

    suspend fun getActivitiesOfLovers(loverIds: Set<Long>): List<NewsFeedItemResponse> {
        val start = System.currentTimeMillis()
        val result = newsFeedRepository.findLastLimitOfLoverIdsIn(200, loverIds)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .map { newsFeedItemConverter.dtoToResponse(it) }
            .toList()
        logger.info { "Returned NewsFeedItems for lovers '$loverIds' in ${System.currentTimeMillis() - start} ms" }
        return result
    }

    suspend fun getWholeFeed(): List<NewsFeedItemResponse> {
        return if (!cacheFilled.getAndSet(true)) {
            fillCacheFromDatabase()
        } else {
            logger.info { "Returning processedFeedCache with size: ${processedFeedCache.size}" }
            ArrayList(processedFeedCache)
        }.map { newsFeedItemConverter.dtoToResponse(it) }
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
        cacheFilled.set(true)

        val processedFeed = newsFeedProcessor.getProcessedFeed()
        processedFeedCache.clear()
        processedFeedCache.addAll(processedFeed)

        return processedFeed
    }

    fun removeFromCache(type: NewsFeedItemDto.Type, referenceId: Long) {
        processedFeedCache.removeIf { it.referenceId == referenceId && it.type == type }
    }
}