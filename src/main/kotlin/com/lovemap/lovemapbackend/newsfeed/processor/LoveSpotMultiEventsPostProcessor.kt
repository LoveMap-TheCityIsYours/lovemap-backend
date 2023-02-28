package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto.ProcessedType.*
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class LoveSpotMultiEventsPostProcessor : NewsFeedPostProcessor<Unit> {
    private val logger = KotlinLogging.logger {}

    override fun processNewsFeed(
        newsFeed: Collection<ProcessedNewsFeedItemDto>,
        context: Unit
    ): List<ProcessedNewsFeedItemDto> {
        logger.info { "Processing NewsFeed of size: '${newsFeed.size}'" }

        val processedFeed = newsFeed.fold(ArrayList()) { combined: ArrayList<ProcessedNewsFeedItemDto>,
                                                         next: ProcessedNewsFeedItemDto ->
            val current: ProcessedNewsFeedItemDto? = combined.lastOrNull()
            if (current != null) {
                val newsFeedItem = if (bothAreSameLoveSpotEvents(current, next)) {
                    if (current.processedType != LOVE_SPOT_MULTI_EVENTS && next.processedType != LOVE_SPOT_MULTI_EVENTS) {
                        combined.removeLast()
                        mergeTwoLoveSpotEvents(current, next)
                    } else if (currentIsMergedMultiLoveSpot(current, next)) {
                        combined.removeLast()
                        mergeMultiLoveSpotAndOther(current, next)
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

    private fun bothAreSameLoveSpotEvents(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return current.processedData.loveSpotId() != null
                && current.processedData.loveSpotId() == next.processedData.loveSpotId()
                && isSupportedType(current) && isSupportedType(next)
    }

    private fun isSupportedType(dto: ProcessedNewsFeedItemDto) =
        dto.processedType == LOVE_SPOT || dto.processedType == LOVE || dto.processedType == LOVE_SPOT_REVIEW || dto.processedType == LOVE_SPOT_PHOTO || dto.processedType == LOVE_SPOT_MULTI_EVENTS

    private fun mergeTwoLoveSpotEvents(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): ProcessedNewsFeedItemDto {

        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = LOVE_SPOT_MULTI_EVENTS,
            processedData = LoveSpotMultiEventsNewsFeedData(
                sortedSetOf(current.processedData, next.processedData)
            ),
            origins = listOf(current, next)
        )
    }

    private fun currentIsMergedMultiLoveSpot(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return current.processedType == LOVE_SPOT_MULTI_EVENTS && isSupportedType(next)
    }

    private fun mergeMultiLoveSpotAndOther(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): ProcessedNewsFeedItemDto {
        val currentData = current.processedData as LoveSpotMultiEventsNewsFeedData
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = LOVE_SPOT_MULTI_EVENTS,
            processedData = LoveSpotMultiEventsNewsFeedData(
                loveSpotEvents = currentData.loveSpotEvents.apply { add(next.processedData) }
            ),
            origins = current.origins + next
        )
    }
}