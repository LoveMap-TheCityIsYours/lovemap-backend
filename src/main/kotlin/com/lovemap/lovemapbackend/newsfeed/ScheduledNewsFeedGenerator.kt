package com.lovemap.lovemapbackend.newsfeed

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedGeneration
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedGenerationRepository
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.provider.NewsFeedProvider
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class ScheduledNewsFeedGenerator(
    private val newsFeedProviders: List<NewsFeedProvider>,
    private val objectMapper: ObjectMapper,
    private val newsFeedService: NewsFeedService,
    private val newsFeedRepository: NewsFeedRepository,
    private val generationRepository: NewsFeedGenerationRepository
) {
    private val logger = KotlinLogging.logger {}

    companion object {
//        private const val INITIAL_DELAY_MINUTES: Long = 5
        private const val INITIAL_DELAY_MINUTES: Long = 0
        private const val REFRESH_RATE_MINUTES: Long = 15
    }

    @Scheduled(initialDelay = INITIAL_DELAY_MINUTES, fixedRate = REFRESH_RATE_MINUTES, timeUnit = TimeUnit.MINUTES)
    fun generateNewsFeedBatch() {
        mono {
            val generationTime = Instant.now()
            logger.info { "Executing generateNewsFeedBatch with generationTime: [$generationTime]" }
            generationRepository.getLastGeneration()?.let { last ->
                if (isItTimeToGenerate(generationTime, last.generatedAt.toInstant())) {
                    generateBatchFrom(generationTime, last.generatedAt.toInstant())
                } else {
                    newsFeedService.reloadCache()
                }
            } ?: run {
                generateBatchFrom(generationTime, generationTime.minus(Duration.ofDays(90)))
            }
        }.subscribe()
    }

    private suspend fun generateBatchFrom(generationTime: Instant, generateFrom: Instant) {
        val savedGeneration: NewsFeedGeneration = initNewsFeedGeneration(generationTime, generateFrom)
        val generationStarted = System.currentTimeMillis()

        val newsFeedItemList: List<NewsFeedItem> = generateBatchAndUpdateCache(generationTime, generateFrom)

        generationRepository.save(savedGeneration.apply {
            generatedItems = newsFeedItemList.size.toLong()
            generationDurationMs = System.currentTimeMillis() - generationStarted
        })
    }

    private suspend fun initNewsFeedGeneration(
        generationTime: Instant,
        generateFrom: Instant
    ): NewsFeedGeneration {
        val savedGeneration: NewsFeedGeneration = generationRepository.save(
            NewsFeedGeneration(generatedAt = Timestamp.from(generationTime))
        )
        logger.info {
            "Generating NewsFeed from [${
                ZonedDateTime.ofInstant(
                    generateFrom, ZoneId.of("UTC")
                )
            }], [${
                ZonedDateTime.ofInstant(
                    generateFrom, ZoneId.systemDefault()
                )
            }]"
        }
        return savedGeneration
    }

    private suspend fun generateBatchAndUpdateCache(
        generationTime: Instant,
        generateFrom: Instant
    ): List<NewsFeedItem> {
        val newlyGeneratedFeed: SortedSet<NewsFeedItemDto> = newsFeedProviders.map {
            it.getNewsFeedFrom(generationTime, generateFrom)
        }.flatMapTo(TreeSet()) { it.toList() }

        val savedNewItems: List<NewsFeedItem> = saveNewlyGeneratedFeed(newlyGeneratedFeed)

        newsFeedService.reloadCache()

        return savedNewItems
    }

    private suspend fun saveNewlyGeneratedFeed(newlyGeneratedFeed: SortedSet<NewsFeedItemDto>): List<NewsFeedItem> {
        val newsFeedItemList: List<NewsFeedItem> = newlyGeneratedFeed.map { it.toNewsFeedItem(objectMapper) }
        logger.info { "Generated '${newsFeedItemList.size}' NewsFeedItems" }
        val saved = newsFeedItemList.mapNotNull {
            runCatching { newsFeedRepository.save(it) }
                .onFailure { e ->
                    logger.warn(e) { "Failed to save NewsFeedItem" }
                }
                .getOrNull()
        }
        logger.info { "Saved '${saved.size}' NewsFeedItems" }
        return newsFeedItemList
    }

    private fun isItTimeToGenerate(generationTime: Instant, lastGeneratedAt: Instant) =
        lastGeneratedAt.plus(Duration.ofMinutes(REFRESH_RATE_MINUTES - 1))
            .isBefore(generationTime)
}