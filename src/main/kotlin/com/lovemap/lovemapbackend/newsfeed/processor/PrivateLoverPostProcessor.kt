package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.data.LoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto.ProcessedType.LOVER
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto.ProcessedType.PRIVATE_LOVERS
import com.lovemap.lovemapbackend.newsfeed.processor.PrivateLoverPostProcessor.Context
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PrivateLoverPostProcessor : NewsFeedPostProcessor<Context> {
    private val logger = KotlinLogging.logger {}

    override suspend fun processNewsFeed(
        newsFeed: Collection<ProcessedNewsFeedItemDto>,
        context: Context
    ): List<ProcessedNewsFeedItemDto> {
        logger.info { "Processing NewsFeed of size: '${newsFeed.size}'" }

        val processedFeed = newsFeed.fold(ArrayList()) { combined: ArrayList<ProcessedNewsFeedItemDto>,
                                                         next: ProcessedNewsFeedItemDto ->
            val current: ProcessedNewsFeedItemDto? = combined.lastOrNull()
            if (current != null) {
                val newsFeedItem = if (bothArePrivateLovers(context, current, next)) {
                    if (current.processedType == LOVER && next.processedType == LOVER) {
                        combined.removeLast()
                        mergeTwoLovers(current, next, context)
                    } else if (currentIsMergedPrivateLovers(current, next)) {
                        combined.removeLast()
                        mergePrivateLoversAndLover(current, next, context)
                    } else {
                        next
                    }
                } else if (nextIsPrivateLover(context, next)) {
                    initPrivateLovers(next, context)
                } else {
                    next
                }
                combined.add(newsFeedItem)
            } else {
                val newsFeedItem = if (nextIsPrivateLover(context, next)) {
                    initPrivateLovers(next, context)
                } else {
                    next
                }
                combined.add(newsFeedItem)
            }
            combined
        }
        logger.info { "Returning Processed NewsFeed of size: '${processedFeed.size}'" }
        return processedFeed
    }

    private fun bothArePrivateLovers(
        context: Context,
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return context.privateLovers.contains(current.referenceId)
                && context.privateLovers.contains(next.referenceId)
    }

    private fun currentIsMergedPrivateLovers(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return current.processedType == PRIVATE_LOVERS && next.processedType == LOVER
    }

    private fun nextIsPrivateLover(
        context: Context,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return next.processedType == LOVER && context.privateLovers.contains(next.referenceId)
    }

    private fun mergeTwoLovers(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto,
        context: Context
    ): ProcessedNewsFeedItemDto {
        logger.debug { "Merging 2 Lovers into 1 PrivateLovers" }
        val currentLover = current.processedData as LoverNewsFeedData
        val currentData = currentLover.copy(
            userName = context.privateLovers[current.referenceId] ?: currentLover.userName
        )
        val nextLover = next.processedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName = context.privateLovers[next.referenceId] ?: nextLover.userName
        )
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = PRIVATE_LOVERS,
            processedData = PrivateLoversNewsFeedData(sortedSetOf(currentData, nextData)),
            origins = listOf(current, next)
        )
    }

    private fun mergePrivateLoversAndLover(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto,
        context: Context
    ): ProcessedNewsFeedItemDto {
        logger.debug { "Merging Lover into PrivateLovers" }
        val currentData = current.processedData as PrivateLoversNewsFeedData
        val nextLover = next.processedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName = context.privateLovers[next.referenceId] ?: nextLover.userName
        )
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = PRIVATE_LOVERS,
            processedData = PrivateLoversNewsFeedData(
                lovers = currentData.lovers.apply { add(nextData) }
            ),
            origins = current.origins + next
        )
    }

    private fun initPrivateLovers(
        current: ProcessedNewsFeedItemDto,
        context: Context
    ): ProcessedNewsFeedItemDto {
        logger.debug { "Initializing PrivateLovers with current item" }
        val currentLover = current.processedData as LoverNewsFeedData
        val currentData = currentLover.copy(
            userName = context.privateLovers[current.referenceId] ?: currentLover.userName
        )
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = PRIVATE_LOVERS,
            processedData = PrivateLoversNewsFeedData(sortedSetOf(currentData)),
            origins = listOf(current)
        )
    }

    data class Context(
        val privateLovers: Map<Long, String>
    )
}
