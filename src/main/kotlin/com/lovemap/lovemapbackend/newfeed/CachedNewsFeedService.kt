package com.lovemap.lovemapbackend.newfeed

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemResponse
import com.lovemap.lovemapbackend.utils.ValidatorService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Service
class CachedNewsFeedService(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val validatorService: ValidatorService,
    private val repository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}
    private val limit = 200
    private val cache = CopyOnWriteArrayList<NewsFeedItemDto>()

    suspend fun updateCache(freshFeed: SortedSet<NewsFeedItemDto>) {
        logger.info { "Updating Cache with fresh NewsFeed data with size: ${freshFeed.size}" }

        val itemDtoList = repository.findLastLimit(limit)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toSet(TreeSet()) as TreeSet
        logger.info { "Fetched last NewsFeedItems from DB with size: ${itemDtoList.size}" }

        val mergedFeed = itemDtoList.apply { addAll(freshFeed) }.take(limit)
        cache.clear()
        cache.addAll(mergedFeed)
        logger.info { "Merged fresh + last stored NewsFeed data for a combined cache with size of: ${mergedFeed.size}" }
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
        val pageItems = cache.subList(page * size, page * size + size)
        logger.info { "Returning page with size: ${pageItems.size}" }
        return pageItems.map { newsFeedItemConverter.dtoToResponse(it) }
    }

    private suspend fun fillCacheFromDatabase(): List<NewsFeedItemDto> {
        logger.info { "Updating Cache because it's empty" }
        val lastStoredFeed = repository.findLastLimit(limit)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toSet(TreeSet())
        logger.info { "Fetched last stored feed with size: ${lastStoredFeed.size}" }
        cache.clear()
        cache.addAll(lastStoredFeed)
        return lastStoredFeed.toList()
    }
}