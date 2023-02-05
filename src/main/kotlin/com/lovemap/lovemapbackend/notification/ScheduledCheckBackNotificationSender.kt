package com.lovemap.lovemapbackend.notification

import com.lovemap.lovemapbackend.tracking.UserTrackingService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.MINUTES

@Component
class ScheduledCheckBackNotificationSender(
    private val notificationService: NotificationService,
    private val userTrackingService: UserTrackingService,
) {
    private val logger = KotlinLogging.logger {}

    //    private val notificationPeriod: Duration = Duration.ofSeconds(5)
    private val notificationPeriod: Duration = Duration.ofDays(3)

    //    @Scheduled(fixedRate = 10, timeUnit = SECONDS)
    @Scheduled(initialDelay = INITIAL_DELAY_MINUTES, fixedRate = SCHEDULE_RATE_MINUTES, timeUnit = MINUTES)
    fun sendNotifications() {
        val notificationThreshold: Instant = Instant.now().minus(notificationPeriod)
        mono {
            val toBeNotifiedLovers = userTrackingService
                .findUnnotifiedInactivesBefore(notificationThreshold)
                .toList()
            notificationService.sendInactivityNotifications(toBeNotifiedLovers)
        }.subscribe()
    }

    companion object {
        private const val INITIAL_DELAY_MINUTES: Long = 15
        private const val SCHEDULE_RATE_MINUTES: Long = 30
    }
}