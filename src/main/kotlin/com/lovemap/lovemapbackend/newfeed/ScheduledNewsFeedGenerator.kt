package com.lovemap.lovemapbackend.newfeed

import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import java.sql.Timestamp
import java.time.Instant

//@Component
class ScheduledNewsFeedGenerator(
    private val newsFeedRepository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}

    //    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    fun generateNewsFeedBatch() {
        mono {
            logger.info { "Executing generateNewsFeedBatch" }
            val newsFeedItem = NewsFeedItem(
                generatedAt = Timestamp.from(Instant.now()),
                type = NewsFeedItem.Type.LOVER,
                referenceId = 1,
                data = "{}"
            )
            newsFeedRepository.save(newsFeedItem)
        }.subscribe()
    }
}