package com.lovemap.lovemapbackend.newfeed.processor

import com.lovemap.lovemapbackend.newfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.MultiLoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.processor.PublicLoverPostProcessor.Context
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
                        mergeTwoLovers(current, next)
                    } else if (current.type == NewsFeedItemDto.Type.MULTI_LOVER && next.type == NewsFeedItemDto.Type.LOVER) {
                        combined.removeLast()
                        mergeMultiLoverAndLover(current, next)
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
        return context.publicLoverIds.contains(current.referenceId) && context.publicLoverIds.contains(next.referenceId)
    }

    private fun maximumMultiLoversReached(current: NewsFeedItemDto) =
        current.type == NewsFeedItemDto.Type.MULTI_LOVER
                && (current.newsFeedData as MultiLoverNewsFeedData).lovers.size == MAXIMUM_MULTI_LOVERS

    private fun mergeTwoLovers(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto
    ): NewsFeedItemDto {
        logger.info { "Merging 2 Lovers into 1 MultiLover" }
        val currentData = current.newsFeedData as LoverNewsFeedData
        val nextData = next.newsFeedData as LoverNewsFeedData
        return current.copy(
            type = NewsFeedItemDto.Type.MULTI_LOVER,
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
        val publicLoverIds: Set<Long>
    )
}
