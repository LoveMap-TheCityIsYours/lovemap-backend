package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.model.*
import com.lovemap.lovemapbackend.newsfeed.model.ProcessedNewsFeedItemDto.ProcessedType.MULTI_LOVER
import com.lovemap.lovemapbackend.newsfeed.processor.PublicLoverPostProcessor.Context
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PublicLoverPostProcessor : NewsFeedPostProcessor<Context> {
    private val logger = KotlinLogging.logger {}

    override fun processNewsFeed(
        newsFeed: Collection<NewsFeedItemDto>,
        context: Context
    ): List<NewsFeedItemDto> {
        logger.info { "Processing NewsFeed of size: '${newsFeed.size}'" }

        val processedFeed = newsFeed.fold(ArrayList()) { combined: ArrayList<NewsFeedItemDto>,
                                                         next: NewsFeedItemDto ->
            val current: NewsFeedItemDto? = combined.lastOrNull()
            if (current != null) {
                val newsFeedItem = if (maximumMultiLoversReached(current)) {
                    logger.info { "Max number of MultiLovers reached: $MAXIMUM_MULTI_LOVERS" }
                    next
                } else if (bothArePublicLovers(context, current, next)) {
                    if (current.type == NewsFeedItemDto.Type.LOVER && next.type == NewsFeedItemDto.Type.LOVER) {
                        combined.removeLast()
                        mergeTwoLovers(current, next, context)
                    } else if (currentIsMergedMultiLover(current, next)) {
                        combined.removeLast()
                        mergeMultiLoverAndLover(current as ProcessedNewsFeedItemDto, next, context)
                    } else {
                        next
                    }
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
        return processedFeed
    }

    private fun bothArePublicLovers(
        context: Context,
        current: NewsFeedItemDto,
        next: NewsFeedItemDto
    ): Boolean {
        return context.publicLovers.containsKey(current.referenceId) && context.publicLovers.containsKey(next.referenceId)
    }

    private fun currentIsMergedMultiLover(
        current: NewsFeedItemDto?,
        next: NewsFeedItemDto
    ): Boolean {
        return current is ProcessedNewsFeedItemDto
                && current.processedType == MULTI_LOVER
                && next.type == NewsFeedItemDto.Type.LOVER
    }

    private fun maximumMultiLoversReached(current: NewsFeedItemDto) =
        current is ProcessedNewsFeedItemDto
                && current.processedType == MULTI_LOVER
                && (current.processedData as MultiLoverNewsFeedData).lovers.size == MAXIMUM_MULTI_LOVERS

    private fun mergeTwoLovers(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.debug { "Merging 2 Lovers into 1 MultiLover" }
        val currentLover = current.newsFeedData as LoverNewsFeedData
        val currentData = currentLover.copy(
            userName = context.publicLovers[current.referenceId] ?: currentLover.userName
        )
        val nextLover = next.newsFeedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName = context.publicLovers[next.referenceId] ?: nextLover.userName
        )
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = MULTI_LOVER,
            processedData = MultiLoverNewsFeedData(sortedSetOf(currentData, nextData)),
            origins = listOf(current, next)
        )
    }

    private fun mergeMultiLoverAndLover(
        current: ProcessedNewsFeedItemDto,
        next: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.debug { "Merging Lover into MultiLover" }
        val currentData = current.newsFeedData as MultiLoverNewsFeedData
        val nextLover = next.newsFeedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName = context.publicLovers[next.referenceId] ?: nextLover.userName
        )
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = MULTI_LOVER,
            processedData = MultiLoverNewsFeedData(
                lovers = currentData.lovers.apply { add(nextData) }
            ),
            origins = current.origins + next
        )
    }

    companion object {
        private const val MAXIMUM_MULTI_LOVERS = 6
    }

    data class Context(
        val publicLovers: Map<Long, String>
    )
}
