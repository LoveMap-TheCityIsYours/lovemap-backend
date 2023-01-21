package com.lovemap.lovemapbackend.newfeed.processor

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lover.LoverRepository
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class NewsFeedProcessor(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val publicLoverProcessor: PublicLoverPostProcessor,
    private val privateLoverProcessor: PrivateLoverPostProcessor,
    private val cachedLoverService: CachedLoverService,
    private val loverRepository: LoverRepository,
    private val repository: NewsFeedRepository,
) {
    private val logger = KotlinLogging.logger {}
    private val limitNotLovers = 200
    private val limitLovers = 200
    private val limitTotal = 300

    suspend fun getProcessedFeed(): List<NewsFeedItemDto> {
        val start = System.currentTimeMillis()
        logger.info { "Starting NewsFeed processing" }

        val loversFeed: Map<Long, NewsFeedItemDto> = fetchOnlyLoversFeed()
        logger.info { "Fetched Lover NewsFeedItems: ${loversFeed.size}" }

        val publicLoverIds: Set<Long> = filterPublicLoverIdsFromIds(loversFeed.keys)
        logger.info { "Fetched Public Lovers: ${publicLoverIds.size}" }

        val privateLoverIds: Set<Long> = loversFeed.keys.subtract(publicLoverIds)
        logger.info { "Fetched Private Lovers: ${privateLoverIds.size}" }

        val notLoversFeed = fetchNotLoversFeed()
        logger.info { "Fetched Not-Lover NewsFeedItems: ${notLoversFeed.size}" }

        val mergedFeed: TreeSet<NewsFeedItemDto> = notLoversFeed.apply { addAll(loversFeed.values) }

        logger.info { "Merged Not-Lover + Lover NewsFeedItems: ${mergedFeed.size}" }

        var processedFeed: List<NewsFeedItemDto> =
            publicLoverProcessor.processNewsFeed(mergedFeed, PublicLoverPostProcessor.Context(publicLoverIds))

        processedFeed =
            privateLoverProcessor.processNewsFeed(processedFeed, PrivateLoverPostProcessor.Context(privateLoverIds))

        // for now we do not process the feed, just return it (waiting for more android rollout)
        logger.info { "Finished NewsFeed processing in ${System.currentTimeMillis() - start} ms." }
        return processedFeed.take(limitTotal)
    }

    private suspend fun fetchNotLoversFeed(): TreeSet<NewsFeedItemDto> {
        return repository
            .findLastLimitNotLovers(limitNotLovers)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toSet(TreeSet()) as TreeSet<NewsFeedItemDto>
    }

    private suspend fun fetchOnlyLoversFeed(): Map<Long, NewsFeedItemDto> {
        val newsFeedItems = repository.findLastLimitOnlyLovers(limitLovers).toList()
        val loverIds = newsFeedItems.map { it.loverId }.toSet()
        logger.info { "Storing '${loverIds.size}' Lovers in Lover Cache for faster processing" }
        loverRepository.findAllById(loverIds).collect {
            cachedLoverService.put(it)
        }

        return newsFeedItems
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toList()
            .associateBy { it.referenceId }
    }

    private suspend fun filterPublicLoverIdsFromIds(loverIds: Set<Long>): Set<Long> {
        return loverRepository
            .findAllByIdInAndPublicProfile(loverIds, true)
            .map { it.id }
            .toSet()
    }
}