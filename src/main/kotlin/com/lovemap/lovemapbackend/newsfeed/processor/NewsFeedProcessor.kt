package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lover.LoverRepository
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedResponseConverter
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
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
    private val loveSpotMultiEventsPostProcessor: LoveSpotMultiEventsPostProcessor,
    private val cachedLoverService: CachedLoverService,
    private val loverRepository: LoverRepository,
    private val repository: NewsFeedRepository,
) {
    private val logger = KotlinLogging.logger {}
    private val limitNotLovers = 200
    private val limitLovers = 200
    private val limitTotal = 300

    suspend fun getProcessedFeed(): List<ProcessedNewsFeedItemDto> {
        val start = System.currentTimeMillis()
        logger.info { "Starting NewsFeed processing v2" }

        val loversFeed: Map<Long, NewsFeedItemDto> = fetchOnlyLoversFeed()
        logger.info { "Fetched Lover NewsFeedItems: ${loversFeed.size}" }

        val publicLovers: Map<Long, String> = filterPublicLoversFromIds(loversFeed.keys)
        logger.info { "Fetched Public Lovers: ${publicLovers.size}" }

        val privateLovers: Map<Long, String> = filterPrivateLoversFromIds(loversFeed.keys)
        logger.info { "Fetched Private Lovers: ${privateLovers.size}" }

        val notLoversFeed = fetchNotLoversFeed()
        logger.info { "Fetched Not-Lover NewsFeedItems: ${notLoversFeed.size}" }

        val mergedFeed: List<ProcessedNewsFeedItemDto> = notLoversFeed.apply { addAll(loversFeed.values) }
            .map { ProcessedNewsFeedItemDto.of(it) }

        logger.info { "Merged Not-Lover + Lover NewsFeedItems: ${mergedFeed.size}" }

        var processedFeed: List<ProcessedNewsFeedItemDto> =
            publicLoverProcessor.processNewsFeed(mergedFeed, PublicLoverPostProcessor.Context(publicLovers))

        processedFeed =
            privateLoverProcessor.processNewsFeed(processedFeed, PrivateLoverPostProcessor.Context(privateLovers))

        processedFeed = loveSpotMultiEventsPostProcessor.processNewsFeed(processedFeed, Unit)

        // for now we do not process the feed, just return it (waiting for more android rollout)
        logger.info { "Finished NewsFeed processing v2 in ${System.currentTimeMillis() - start} ms." }
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

    private suspend fun filterPublicLoversFromIds(loverIds: Set<Long>): Map<Long, String> {
        return loverRepository
            .findAllByIdInAndPublicProfile(loverIds, true)
            .toList()
            .associateBy({ it.id }, { it.displayName })
    }

    private suspend fun filterPrivateLoversFromIds(loverIds: Set<Long>): Map<Long, String> {
        return loverRepository
            .findAllByIdInAndPublicProfile(loverIds, false)
            .toList()
            .associateBy({ it.id }, { it.displayName })
    }
}