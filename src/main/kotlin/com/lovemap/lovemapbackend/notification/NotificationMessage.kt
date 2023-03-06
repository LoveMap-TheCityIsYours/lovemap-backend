package com.lovemap.lovemapbackend.notification

data class NotificationMessage(
    val type: NotificationType
)

enum class NotificationType {
    COME_BACK_PLEASE,
    NEW_LOVE_SPOT,
    NEW_LOVE_SPOT_PHOTO,
    NEW_LOVE_SPOT_REVIEW,
    NEW_PUBLIC_LOVER,

    NEW_REVIEW_ON_YOUR_LOVE_SPOT,
    NEW_LIKE_ON_YOUR_PHOTO,
    NEW_DISLIKE_ON_YOUR_PHOTO,
    NEW_FOLLOWER,
    PARTNERSHIP_REQUESTED,
    PARTNERSHIP_ACCEPTED,
    PARTNERSHIP_DENIED,
}
