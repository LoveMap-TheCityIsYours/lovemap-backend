package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.newsfeed.data.LoveSpotNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.dataparser.LoveSpotNewsFeedDataParser
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto.ProcessedType.*
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import mu.KotlinLogging
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component

@Component
class LoveSpotMultiEventsPostProcessor(
    private val repository: NewsFeedRepository,
    private val loveSpotNewsFeedDataParser: LoveSpotNewsFeedDataParser,
    private val cachedLoverService: CachedLoverService
) : NewsFeedPostProcessor<Unit> {

    private val logger = KotlinLogging.logger {}

    private val loveSpotType = NewsFeedItem.Type.LOVE_SPOT

    override suspend fun processNewsFeed(
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
                    } else if (currentMultiLoveSpotShouldBeMergedWithNext(current, next)) {
                        combined.removeLast()
                        mergeMultiLoveSpotAndOther(current, next)
                    } else {
                        next
                    }
                } else if (currentMultiLoveSpotShouldBeFinished(current, next)) {
                    finishCurrentMultiLoveSpot(current)
                    next
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

    private suspend fun mergeTwoLoveSpotEvents(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): ProcessedNewsFeedItemDto {
        val loveSpotData: LoveSpotNewsFeedData = findLoveSpotData(current)
        val multiEventsData = LoveSpotMultiEventsNewsFeedData(
            loveSpot = loveSpotData
        ).addProcessedData(current.processedData).addProcessedData(next.processedData)
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = LOVE_SPOT_MULTI_EVENTS,
            processedData = multiEventsData,
            origins = listOf(current, next)
        )
    }

    private suspend fun findLoveSpotData(current: ProcessedNewsFeedItemDto): LoveSpotNewsFeedData {
        val loveSpotId: Long = current.processedData.loveSpotId() ?: throw LoveMapException(
            INTERNAL_SERVER_ERROR,
            ErrorCode.LoveSpotNotFound,
            "loveSpotId", "is null"
        )

        val newsFeedItem = loveSpotId.let {
            repository.findByTypeAndReferenceId(loveSpotType, it)
        } ?: throw LoveMapException(
            INTERNAL_SERVER_ERROR,
            ErrorCode.LoveSpotNotFound,
            loveSpotId.toString(),
            "NewsFeedItem not found for loveSpotId. NewsFeedItem: $current"
        )

        return loveSpotNewsFeedDataParser.parse(newsFeedItem.data)
    }

    private fun currentMultiLoveSpotShouldBeMergedWithNext(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return current.processedType == LOVE_SPOT_MULTI_EVENTS && isSupportedType(next)
    }

    private fun currentMultiLoveSpotShouldBeFinished(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): Boolean {
        return current.processedType == LOVE_SPOT_MULTI_EVENTS &&
                (!isSupportedType(next) ||
                        current.processedData.loveSpotId() != next.processedData.loveSpotId())
    }

    private fun mergeMultiLoveSpotAndOther(
        current: ProcessedNewsFeedItemDto,
        next: ProcessedNewsFeedItemDto
    ): ProcessedNewsFeedItemDto {
        val currentData = current.processedData as LoveSpotMultiEventsNewsFeedData
        return ProcessedNewsFeedItemDto(
            delegate = current,
            processedType = LOVE_SPOT_MULTI_EVENTS,
            processedData = currentData.addProcessedData(next.processedData),
            origins = current.origins + next
        )
    }

    private suspend fun finishCurrentMultiLoveSpot(current: ProcessedNewsFeedItemDto) {
        val currentData = current.processedData as LoveSpotMultiEventsNewsFeedData
        val lovers = currentData.getLoverIds()
            .mapNotNull { cachedLoverService.getCachedLoverById(it) }
        currentData.addLovers(lovers)
    }

}
