package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
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
            val current = combined.lastOrNull()
            if (current != null) {
                val newsFeedItem = if (maximumMultiLoversReached(current)) {
                    logger.info { "Max number of MultiLovers reached: $MAXIMUM_MULTI_LOVERS" }
                    next
                } else if (bothArePublicLovers(context, current, next)) {
                    if (current.type == NewsFeedItemDto.Type.LOVER && next.type == NewsFeedItemDto.Type.LOVER) {
                        combined.removeLast()
                        mergeTwoLovers(current, next, context)
                    } else if (current.type == NewsFeedItemDto.Type.MULTI_LOVER && next.type == NewsFeedItemDto.Type.LOVER) {
                        combined.removeLast()
                        mergeMultiLoverAndLover(current, next, context)
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

    private fun maximumMultiLoversReached(current: NewsFeedItemDto) =
        current.type == NewsFeedItemDto.Type.MULTI_LOVER
                && (current.newsFeedData as MultiLoverNewsFeedData).lovers.size == MAXIMUM_MULTI_LOVERS

    private fun mergeTwoLovers(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.info { "Merging 2 Lovers into 1 MultiLover" }
        val currentLover = current.newsFeedData as LoverNewsFeedData
        val currentData = currentLover.copy(
            userName = context.publicLovers[current.referenceId] ?: currentLover.userName
        )
        val nextLover = next.newsFeedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName =  context.publicLovers[next.referenceId] ?: nextLover.userName
        )
        return current.copy(
            type = NewsFeedItemDto.Type.MULTI_LOVER,
            newsFeedData = MultiLoverNewsFeedData(sortedSetOf(currentData, nextData))
        )
    }

    private fun mergeMultiLoverAndLover(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.info { "Merging Lover into MultiLover" }
        val currentData = current.newsFeedData as MultiLoverNewsFeedData
        val nextLover = next.newsFeedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName =  context.publicLovers[next.referenceId] ?: nextLover.userName
        )
        return current.copy(
            type = NewsFeedItemDto.Type.MULTI_LOVER,
            newsFeedData = MultiLoverNewsFeedData(
                lovers = currentData.lovers.apply { add(nextData) }
            )
        )
    }

    companion object {
        private const val MAXIMUM_MULTI_LOVERS = 6
    }

    data class Context(
        val publicLovers: Map<Long, String>
    )
}
