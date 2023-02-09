package com.lovemap.lovemapbackend.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.tracking.UserTrack
import com.lovemap.lovemapbackend.tracking.UserTrackingService
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class NotificationService(
    private val firebaseApp: FirebaseApp,
    private val asyncTaskService: AsyncTaskService,
    private val userTrackingService: UserTrackingService
) {
    private val logger = KotlinLogging.logger {}

    private val notificationRadiusMeters: Long = 50_000

    suspend fun sendLoveSpotNotification(loveSpot: LoveSpot, type: NotificationType) {
        asyncTaskService.runAsync {
            runCatching {
                val usersToNotify = userTrackingService
                    .findUsersWithinMetersOf(loveSpot.latitude, loveSpot.longitude, notificationRadiusMeters)
                    .toList()

                val loverIds = usersToNotify.map { it.loverId }

                logger.info { "Preparing LoveSpot '${loveSpot.id}' notifications '$type' for lovers '$loverIds'" }

                if (loverIds.isNotEmpty()) {
                    val multicastMessage = MulticastMessage.builder()
                        .addAllTokens(usersToNotify.mapNotNull { it.firebaseToken })
                        .putData(NOTIFICATION_TYPE, type.name)
                        .putData(LOVE_SPOT_ID, loveSpot.id.toString())
                        .build()

                    FirebaseMessaging.getInstance(firebaseApp).sendMulticast(multicastMessage)
                    logger.info { "LoveSpot '${loveSpot.id}' notification '$type' sent to lovers '$loverIds'" }
                }

            }.onFailure { e ->
                logger.error(e) { "Failed to send LoveSpot '${loveSpot.id}' notification '$type' to lovers" }
            }
        }
    }

    fun sendInactivityNotifications(usersToNotify: List<UserTrack>) {
        asyncTaskService.runBlockingAsync {
            val loverIds = usersToNotify.map { it.loverId }
            logger.info { "Preparing inactivity notifications for lovers '$loverIds'" }

            if (loverIds.isNotEmpty()) {
                runCatching {

                    val multicastMessage = MulticastMessage.builder()
                        .addAllTokens(usersToNotify.mapNotNull { it.firebaseToken })
                        .putData(NOTIFICATION_TYPE, NotificationType.COME_BACK_PLEASE.name)
                        .build()
                    FirebaseMessaging.getInstance(firebaseApp).sendMulticast(multicastMessage)
                    logger.info { "Inactivity notification sent to lovers '$loverIds'" }

                }.onSuccess {
                    userTrackingService.updateActivityNotifications(loverIds, Instant.now())
                }.onFailure { e ->
                    logger.error(e) { "Failed to send inactivity notification to lovers" }
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_TYPE = "notificationType"
        private const val LOVE_SPOT_ID = "loveSpotId"
    }
}