package com.lovemap.lovemapbackend.newfeed

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto.Type.LOVER
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto.Type.MULTI_LOVER
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class NewsFeedProcessor(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val repository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}
    private val limit = 200

    suspend fun processNewlyGeneratedFeed(newlyGenerated: SortedSet<NewsFeedItemDto>): List<NewsFeedItemDto> {
        logger.info { "Processing newly generated NewsFeed of size: '${newlyGenerated.size}'" }

        val feedFromDatabase = repository.findLastLimit(limit)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toSet(TreeSet<NewsFeedItemDto>()) as TreeSet<NewsFeedItemDto>
        logger.info { "Read NewsFeed from Database with size: '${feedFromDatabase.size}'" }

        val mergedFeed = feedFromDatabase.apply { addAll(newlyGenerated) }.take(limit)
        logger.info { "Merged NewsFeed from Database with newly generated NewsFeed with size: '${mergedFeed.size}'" }

        return processFeed(mergedFeed)
    }

    suspend fun getProcessedFeed(): List<NewsFeedItemDto> {
        val lastStoredFeed = repository.findLastLimit(limit)
            .map { newsFeedItemConverter.dtoFromItem(it) }
            .toSet(TreeSet())
        logger.info { "Fetched last stored feed with size: ${lastStoredFeed.size}" }
        return processFeed(lastStoredFeed)
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