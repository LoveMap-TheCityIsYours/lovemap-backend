package com.lovemap.lovemapbackend.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.partnership.Partnership
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhoto
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLike
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.notification.NotificationType.*
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

    suspend fun sendNearbyLoveSpotNotification(loveSpot: LoveSpot, type: NotificationType) {
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
                    logger.info { "LoveSpot '${loveSpot.id}' notifications '$type' sent to lovers '$loverIds'" }
                }

            }.onFailure { e ->
                logger.error(e) { "Failed to send LoveSpot '${loveSpot.id}' notifications '$type' to lovers" }
            }
        }
    }

    suspend fun sendReviewNotification(loveSpot: LoveSpot, review: LoveSpotReview) {
        asyncTaskService.runAsync {
            runCatching {
                userTrackingService.findByLoverId(loveSpot.addedBy)?.firebaseToken?.let {
                    val message = Message.builder()
                        .setToken(it)
                        .putData(NOTIFICATION_TYPE, NEW_REVIEW_ON_YOUR_LOVE_SPOT.name)
                        .putData(LOVE_SPOT_ID, loveSpot.id.toString())
                        .build()

                    FirebaseMessaging.getInstance(firebaseApp).send(message)
                    logger.info {
                        "Notification '$NEW_REVIEW_ON_YOUR_LOVE_SPOT' sent to " +
                                "lover '${loveSpot.addedBy}' for LoveSpot '${loveSpot.id}'"
                    }
                }
            }.onFailure { e ->
                logger.error(e) {
                    "Failed to send '$NEW_REVIEW_ON_YOUR_LOVE_SPOT' to " +
                            "lover '${loveSpot.addedBy}' for LoveSpot '${loveSpot.id}'"
                }
            }
        }
    }

    suspend fun sendPhotoLikeNotification(loveSpotPhoto: LoveSpotPhoto, likeOrDislike: Int) {
        asyncTaskService.runAsync {
            val notificationType = if (likeOrDislike == PhotoLike.LIKE) {
                NEW_LIKE_ON_YOUR_PHOTO
            } else {
                NEW_DISLIKE_ON_YOUR_PHOTO
            }

            runCatching {
                userTrackingService.findByLoverId(loveSpotPhoto.uploadedBy)?.firebaseToken?.let {
                    val message = Message.builder()
                        .setToken(it)
                        .putData(NOTIFICATION_TYPE, notificationType.name)
                        .putData(LOVE_SPOT_ID, loveSpotPhoto.loveSpotId.toString())
                        .build()

                    FirebaseMessaging.getInstance(firebaseApp).send(message)
                    logger.info {
                        "Notification '$notificationType' sent to " +
                                "lover '${loveSpotPhoto.uploadedBy}' for LoveSpot '${loveSpotPhoto.loveSpotId}'"
                    }
                }
            }.onFailure { e ->
                logger.error(e) {
                    "Failed to send '$notificationType' to " +
                            "lover '${loveSpotPhoto.uploadedBy}' for LoveSpot '${loveSpotPhoto.loveSpotId}'"
                }
            }
        }
    }

    suspend fun sendNewFollowerNotification(targetLover: Lover) {
        sendSimpleNotification(targetLover.id, NEW_FOLLOWER)
    }

    suspend fun sendPartnershipRequestedNotification(partnership: Partnership) {
        sendSimpleNotification(partnership.respondentId, PARTNERSHIP_REQUESTED)
    }

    suspend fun sendPartnershipAcceptedNotification(partnership: Partnership) {
        sendSimpleNotification(partnership.initiatorId, PARTNERSHIP_ACCEPTED)
    }

    suspend fun sendPartnershipDeniedNotification(partnership: Partnership) {
        sendSimpleNotification(partnership.initiatorId, PARTNERSHIP_DENIED)
    }

    private suspend fun sendSimpleNotification(loverId: Long, notificationType: NotificationType) {
        asyncTaskService.runAsync {
            runCatching {
                userTrackingService.findByLoverId(loverId)?.firebaseToken?.let {
                    val message = Message.builder()
                        .setToken(it)
                        .putData(NOTIFICATION_TYPE, notificationType.name)
                        .build()

                    FirebaseMessaging.getInstance(firebaseApp).send(message)
                    logger.info {
                        "Notification '$notificationType' sent to lover '$loverId'"
                    }
                }
            }.onFailure { e ->
                logger.error(e) {
                    "Failed to send '$notificationType' to lover '$loverId'"
                }
            }
        }
    }

    suspend fun notifyUsersOfNewPublicLover(caller: Lover, latitude: Double, longitude: Double) {
        if (caller.publicProfile) {
            asyncTaskService.runAsync {
                userTrackingService.findByLoverId(caller.id)?.let { userTrack ->
                    if (!userTrack.haveOthersBeenNotifiedAboutThisPublicUserJoining) {
                        doNotifyUsersOfNewPublicLover(userTrack, latitude, longitude, caller)
                    }
                }
            }
        }
    }

    private suspend fun doNotifyUsersOfNewPublicLover(
        userTrack: UserTrack,
        latitude: Double,
        longitude: Double,
        caller: Lover
    ) {
        runCatching {

            userTrackingService.save(
                userTrack.copy(
                    haveOthersBeenNotifiedAboutThisPublicUserJoining = true
                )
            )

            val usersToNotify = userTrackingService
                .findUsersWithinMetersOf(latitude, longitude, notificationRadiusMeters)
                .toList()

            val loverIds = usersToNotify.map { it.loverId }

            logger.info { "Preparing $NEW_PUBLIC_LOVER notifications for lovers '$loverIds'" }

            if (loverIds.isNotEmpty()) {
                val multicastMessage = MulticastMessage.builder()
                    .addAllTokens(usersToNotify.mapNotNull { it.firebaseToken })
                    .putData(NOTIFICATION_TYPE, NEW_PUBLIC_LOVER.name)
                    .putData(LOVER_ID, caller.id.toString())
                    .build()

                FirebaseMessaging.getInstance(firebaseApp).sendMulticast(multicastMessage)
                logger.info { "$NEW_PUBLIC_LOVER notifications sent to lovers '$loverIds'" }
            }

        }.onFailure { e ->
            logger.error(e) { "Failed to send $NEW_PUBLIC_LOVER notifications.'" }
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
                        .putData(NOTIFICATION_TYPE, COME_BACK_PLEASE.name)
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
        private const val LOVER_ID = "loverId"
    }
}