package com.lovemap.lovemapbackend.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.lovemap.lovemapbackend.notification.NotificationType.COME_BACK_PLEASE
import com.lovemap.lovemapbackend.tracking.UserTrack
import com.lovemap.lovemapbackend.tracking.UserTrackRepository
import com.lovemap.lovemapbackend.tracking.UserTrackingService
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.MINUTES

@Component
class ScheduledCheckBackNotificationSender(
    private val firebaseApp: FirebaseApp,
    private val asyncTaskService: AsyncTaskService,
    private val userTrackingService: UserTrackingService,
    private val userTrackRepository: UserTrackRepository
) {
    private val logger = KotlinLogging.logger {}

    private val notificationPeriod: Duration = Duration.ofDays(3)

    @Scheduled(initialDelay = INITIAL_DELAY_MINUTES, fixedRate = SCHEDULE_RATE_MINUTES, timeUnit = MINUTES)
    fun sendNotifications() {
        val notificationThreshold: Instant = Instant.now().minus(notificationPeriod)
        mono {
            userTrackRepository.findByLastActivityAndNotificationBefore(notificationThreshold).asFlow()
                .collect { sendNotificationAsync(it) }
        }.subscribe()

    }

    private fun sendNotificationAsync(userTrack: UserTrack) {
        asyncTaskService.runBlockingAsync {
            runCatching {
                val message = Message.builder()
                    .setToken(userTrack.firebaseToken)
                    .putData(NOTIFICATION_TYPE, COME_BACK_PLEASE.name)
                    .build()
                FirebaseMessaging.getInstance(firebaseApp).send(message)
                logger.info { "Notification sent to lover '${userTrack.loverId}'" }
            }.onSuccess {
                userTrackingService.updateActivityNotification(userTrack.loverId, Instant.now())
            }.onFailure { e ->
                logger.error(e) { "Failed to send notification to lover '${userTrack.loverId}'" }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_TYPE = "notificationType"
        private const val INITIAL_DELAY_MINUTES: Long = 15
        private const val SCHEDULE_RATE_MINUTES: Long = 30
    }
}