package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.model.PrivateLoversNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.processor.PrivateLoverPostProcessor.Context
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PrivateLoverPostProcessor : NewsFeedPostProcessor<Context> {
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
                val newsFeedItem = if (bothArePrivateLovers(context, current, next)) {
                    if (current.type == NewsFeedItemDto.Type.LOVER && next.type == NewsFeedItemDto.Type.LOVER) {
                        combined.removeLast()
                        mergeTwoLovers(current, next, context)
                    } else if (current.type == NewsFeedItemDto.Type.PRIVATE_LOVERS && next.type == NewsFeedItemDto.Type.LOVER) {
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
                combined.add(next)
            }
            combined
        }
        logger.info { "Returning Processed NewsFeed of size: '${processedFeed.size}'" }
        return processedFeed
    }

    private fun bothArePrivateLovers(
        context: Context,
        current: NewsFeedItemDto,
        next: NewsFeedItemDto
    ): Boolean {
        return context.privateLovers.contains(current.referenceId) && context.privateLovers.contains(next.referenceId)
    }

    private fun nextIsPrivateLover(
        context: Context,
        next: NewsFeedItemDto
    ): Boolean {
        return next.type == NewsFeedItemDto.Type.LOVER && context.privateLovers.contains(next.referenceId)
    }

    private fun mergeTwoLovers(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.info { "Merging 2 Lovers into 1 PrivateLovers" }
        val currentLover = current.newsFeedData as LoverNewsFeedData
        val currentData = currentLover.copy(
            userName = context.privateLovers[current.referenceId] ?: currentLover.userName
        )
        val nextLover = next.newsFeedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName =  context.privateLovers[next.referenceId] ?: nextLover.userName
        )
        return current.copy(
            type = NewsFeedItemDto.Type.PRIVATE_LOVERS,
            newsFeedData = PrivateLoversNewsFeedData(sortedSetOf(currentData, nextData))
        )
    }

    private fun mergePrivateLoversAndLover(
        current: NewsFeedItemDto,
        next: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.info { "Merging Lover into PrivateLovers" }
        val currentData = current.newsFeedData as PrivateLoversNewsFeedData
        val nextLover = next.newsFeedData as LoverNewsFeedData
        val nextData = nextLover.copy(
            userName =  context.privateLovers[next.referenceId] ?: nextLover.userName
        )
        return current.copy(
            type = NewsFeedItemDto.Type.PRIVATE_LOVERS,
            newsFeedData = PrivateLoversNewsFeedData(
                lovers = currentData.lovers.apply { add(nextData) }
            )
        )
    }

    private fun initPrivateLovers(
        current: NewsFeedItemDto,
        context: Context
    ): NewsFeedItemDto {
        logger.info { "Initializing PrivateLovers with current item" }
        val currentLover = current.newsFeedData as LoverNewsFeedData
        val currentData = currentLover.copy(
            userName = context.privateLovers[current.referenceId] ?: currentLover.userName
        )
        return current.copy(
            type = NewsFeedItemDto.Type.PRIVATE_LOVERS,
            newsFeedData = PrivateLoversNewsFeedData(sortedSetOf(currentData))
        )
    }

    data class Context(
        val privateLovers: Map<Long, String>
    )
}
