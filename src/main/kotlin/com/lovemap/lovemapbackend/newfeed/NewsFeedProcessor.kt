package com.lovemap.lovemapbackend.newfeed

import com.lovemap.lovemapbackend.lover.LoverRepository
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto.Type.LOVER
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto.Type.MULTI_LOVER
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class NewsFeedProcessor(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val loverRepository: LoverRepository,
    private val repository: NewsFeedRepository,
) {
    private val logger = KotlinLogging.logger {}
    private val limitNotLovers = 200
    private val limitLovers = 200
    private val limitTotal = 300

    suspend fun getProcessedFeed(): List<NewsFeedItemDto> {
        logger.info { "Starting NewsFeed processing" }

        val loversFeed: Map<Long, NewsFeedItemDto> = fetchOnlyLoversFeed()
        logger.info { "Fetched Lover NewsFeedItems: ${loversFeed.size}" }

        val publicLoverIds: Set<Long> = filterPublicLoverIdsFromIds(loversFeed.keys)
        logger.info { "Fetched Public Lovers: ${publicLoverIds.size}" }

        val privateLoverIds: Set<Long> = loversFeed.keys.subtract(publicLoverIds)
        logger.info { "Fetched Private Lovers: ${privateLoverIds.size}" }

        val publicLoversFeed: Map<Long, NewsFeedItemDto> =
            loversFeed.filter { publicLoverIds.contains(it.key) }

        // TODO: later create new NewsFeedResponse Type: PRIVATE_LOVERS
        // TODO: and collect them like in MULTI_LOVER, but only the number of them, not the names
        // TODO: so this separation wont be needed, it's only temporary and for debugging
        val privateLoversFeed: Map<Long, NewsFeedItemDto> =
            loversFeed.filter { privateLoverIds.contains(it.key) }

        val notLoversFeed = fetchNotLoversFeed()
        logger.info { "Fetched Not-Lover NewsFeedItems: ${notLoversFeed.size}" }

        val mergedFeed: TreeSet<NewsFeedItemDto> =
            notLoversFeed.apply { addAll(publicLoversFeed.values) }

        logger.info { "Merged Not-Lover + Public Lover NewsFeedItems: ${mergedFeed.size}" }

        // for now we do not process the feed, just return it (waiting for more android rollout)
//        return processFeed(mergedFeed)
        return mergedFeed.toList().take(limitTotal)
    }

    private suspend fun fetchNotLoversFeed(): TreeSet<NewsFeedItemDto> {
        return repository
            .findLastLimitNotLovers(limitNotLovers)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toSet(TreeSet()) as TreeSet<NewsFeedItemDto>
    }

    private suspend fun fetchOnlyLoversFeed(): Map<Long, NewsFeedItemDto> {
        return repository
            .findLastLimitOnlyLovers(limitLovers)
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

    private suspend fun processFeed(unprocessedFeed: Collection<NewsFeedItemDto>): List<NewsFeedItemDto> {
        logger.info { "Processing NewsFeed of size: '${unprocessedFeed.size}'" }

        val processedFeed = unprocessedFeed.fold(TreeSet()) { combined: TreeSet<NewsFeedItemDto>,
                                                              next: NewsFeedItemDto ->
            val current = combined.lastOrNull()
            if (current != null) {
                val newsFeedItem = if (maximumMultiLoversReached(current)) {
                    logger.info { "Max number of MultiLovers reached: $MAXIMUM_MULTI_LOVERS" }
                    next
                } else if (current.type == LOVER && next.type == LOVER) {
                    combined.remove(current)
                    mergeTwoLovers(current, next)
                } else if (current.type == MULTI_LOVER && next.type == LOVER) {
                    mergeMultiLoverAndLover(current, next)
                } else if (current.type == MULTI_LOVER && next.type == MULTI_LOVER) {
                    mergeTwoMultiLovers(current, next)
                } else {
                    next
                }
                combined.add(newsFeedItem)
            } else {
                combined.add(next)
            }
            combined
        }
        logger.info { "Returning Processed NewsFeed of size: '${processedFeed.size}'" }
        return processedFeed.toList()
    }

    private fun maximumMultiLoversReached(current: NewsFeedItemDto) =
        current.type == MULTI_LOVER
                && (current.newsFeedData as MultiLoverNewsFeedData).lovers.size == MAXIMUM_MULTI_LOVERS

    private fun mergeTwoLovers(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto
    ): NewsFeedItemDto {
        logger.info { "Merging 2 Lovers into 1 MultiLover" }
        val currentData = current.newsFeedData as LoverNewsFeedData
        val nextData = next.newsFeedData as LoverNewsFeedData
        return current.copy(
            type = MULTI_LOVER,
            newsFeedData = MultiLoverNewsFeedData(sortedSetOf(currentData, nextData))
        )
    }

    private fun mergeMultiLoverAndLover(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto
    ): NewsFeedItemDto {
        logger.info { "Merging Lover into MultiLover" }
        val currentData = current.newsFeedData as MultiLoverNewsFeedData
        val nextData = next.newsFeedData as LoverNewsFeedData
        return current.copy(
            type = MULTI_LOVER,
            newsFeedData = MultiLoverNewsFeedData(
                lovers = currentData.lovers.apply { add(nextData) }
            )
        )
    }

    private fun mergeTwoMultiLovers(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto
    ): NewsFeedItemDto {
        logger.info { "Merging 2 MultiLovers into 1 MultiLover" }
        val currentData = current.newsFeedData as MultiLoverNewsFeedData
        val nextData = next.newsFeedData as MultiLoverNewsFeedData
        return current.copy(
            type = MULTI_LOVER,
            newsFeedData = MultiLoverNewsFeedData(
                lovers = currentData.lovers.apply { addAll(nextData.lovers) }
            )
        )
    }

    companion object {
        private const val MAXIMUM_MULTI_LOVERS = 6
    }
}