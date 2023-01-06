package com.lovemap.lovemapbackend.newfeed

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.lover.LoverService
import com.lovemap.lovemapbackend.lover.wishlist.WishlistService
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewService
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.provider.NewsFeedProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

const val REFRESH_RATE_MINUTES: Long = 15

/*
        LOVE_SPOT,
        LOVE_SPOT_REVIEW,
        LOVE_SPOT_PHOTO,
        LOVE_SPOT_PHOTO_LIKE_DISLIKE,
        LOVE,
        WISHLIST_ITEM,
        LOVER
 */
@Component
class ScheduledNewsFeedGenerator(
    private val newsFeedProviders: List<NewsFeedProvider>,
    private val reviewService: LoveSpotReviewService,
    private val photoService: LoveSpotPhotoService,
    private val wishlistService: WishlistService,
    private val loverService: LoverService,
    private val objectMapper: ObjectMapper,
    private val cachedNewsFeedService: CachedNewsFeedService,
    private val newsFeedRepository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = REFRESH_RATE_MINUTES, timeUnit = TimeUnit.MINUTES)
    fun generateNewsFeedBatch() {
        mono {
            logger.info { "Executing generateNewsFeedBatch" }
            val generationTime = Instant.now()
            newsFeedRepository.findLast()?.let { last ->
                if (isItTimeToGenerate(generationTime, last)) {
                    generateBatchFrom(generationTime, last.generatedAt.toInstant())
                }
            } ?: run {
                generateBatchFrom(generationTime, generationTime.minus(Duration.ofDays(15)))
            }

        }.subscribe()
    }

    private suspend fun generateBatchFrom(generationTime: Instant, generateFrom: Instant) {
        logger.info {
            "Generating new feed from [${
                ZonedDateTime.ofInstant(
                    generateFrom, ZoneId.of("UTC")
                )
            }], [${
                ZonedDateTime.ofInstant(
                    generateFrom, ZoneId.systemDefault()
                )
            }]"
        }

        logger.info { "Starting to generate NewsFeedItems" }
        val completeFeed: SortedSet<NewsFeedItemDto> = newsFeedProviders.map {
            it.getNewsFeedFrom(generationTime, generateFrom)
        }.flatMap { it.toList().toSortedSet() }.toSortedSet()

        cachedNewsFeedService.updateCache(completeFeed)
        val newsFeedItemList = completeFeed.map { it.toNewsFeedItem(objectMapper) }
        logger.info { "Generated '${newsFeedItemList.size}' NewsFeedItems" }
        newsFeedRepository.saveAll(newsFeedItemList).collect()
    }

    private fun isItTimeToGenerate(generationTime: Instant, it: NewsFeedItem) =
        it.generatedAt.toInstant().plus(Duration.ofMinutes(REFRESH_RATE_MINUTES - 1))
            .isBefore(generationTime)
}