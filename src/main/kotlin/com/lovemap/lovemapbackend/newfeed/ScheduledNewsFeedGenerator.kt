package com.lovemap.lovemapbackend.newfeed

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedGeneration
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedGenerationRepository
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.provider.NewsFeedProvider
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

const val REFRESH_RATE_MINUTES: Long = 30

@Component
class ScheduledNewsFeedGenerator(
    private val newsFeedProviders: List<NewsFeedProvider>,
    private val objectMapper: ObjectMapper,
    private val newsFeedService: NewsFeedService,
    private val newsFeedProcessor: NewsFeedProcessor,
    private val newsFeedRepository: NewsFeedRepository,
    private val generationRepository: NewsFeedGenerationRepository
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = REFRESH_RATE_MINUTES, timeUnit = TimeUnit.MINUTES)
    fun generateNewsFeedBatch() {
        mono {
            val generationTime = Instant.now()
            logger.info { "Executing generateNewsFeedBatch with generationTime: [$generationTime]" }
            generationRepository.getLastGeneration()?.let { last ->
                if (isItTimeToGenerate(generationTime, last.generatedAt.toInstant())) {
                    generateBatchFrom(generationTime, last.generatedAt.toInstant())
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

        // must not be called after processing, only before
        val savedNewItems: List<NewsFeedItem> = saveNewlyGeneratedFeed(newlyGeneratedFeed)

        val processedNewsFeed = newsFeedProcessor.processNewlyGeneratedFeed(newlyGeneratedFeed)
        newsFeedService.updateCache(processedNewsFeed)

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