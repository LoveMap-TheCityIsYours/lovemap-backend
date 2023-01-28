package com.lovemap.lovemapbackend.lover.ranking

import com.lovemap.lovemapbackend.lover.LoverRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val REFRESH_RATE_MINUTES: Long = 30
private const val INITIAL_DELAY_MINUTES: Long = 10
private const val HOF_USERS = 1_000

@Component
class ScheduledHallOfFameCalculator(
    private val loverRepository: LoverRepository
) {
    private val logger = KotlinLogging.logger {}

    //    @Scheduled(fixedRate = REFRESH_RATE_MINUTES, timeUnit = TimeUnit.MINUTES)
    @Scheduled(fixedRate = REFRESH_RATE_MINUTES, initialDelay = INITIAL_DELAY_MINUTES, timeUnit = TimeUnit.MINUTES)
    fun recalculateHofPositions() {
        mono {
            val start = System.currentTimeMillis()
            logger.info { "Starting recalculateHofPositions" }

            val positionCounter = AtomicInteger(1)
            val nulled = loverRepository.nullAllHofPositions()
            logger.info { "Nulled HoF positions: $nulled" }
            loverRepository.findTopLimitOrderByPoints(HOF_USERS).map { lover ->
                lover.hallOfFamePosition = positionCounter.getAndIncrement()
                runCatching { loverRepository.save(lover) }.onFailure { e ->
                    logger.error(e) { "Failed to save HoF position for lover '$lover'" }
                }.getOrNull()
            }.collect()

            logger.info { "Finished recalculateHofPositions in ${System.currentTimeMillis() - start} ms." }
        }.subscribe()
    }
}