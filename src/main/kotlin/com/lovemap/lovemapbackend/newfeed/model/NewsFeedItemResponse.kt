package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import java.time.Instant

data class NewsFeedItemResponse(
    val type: NewsFeedItemType,
    val generatedAt: Instant,
    val generatedAtFormatted: String,
    val happenedAt: Instant,
    val happenedAtFormatted: String,
    val referenceId: Long,
    val country: String,
    val loveSpot: LoveSpotNewsFeedResponse? = null,
    val love: LoveNewsFeedResponse? = null,
    val loveSpotReview: LoveSpotReviewNewsFeedResponse? = null,
    val loveSpotPhoto: LoveSpotPhotoNewsFeedResponse? = null,
    val photoLike: PhotoLikeNewsFeedResponse? = null,
    val wishlist: WishlistNewsFeedResponse? = null,
    val lover: LoverNewsFeedResponse? = null
)

enum class NewsFeedItemType {
    LOVE_SPOT,
    LOVE_SPOT_REVIEW,
    LOVE_SPOT_PHOTO,
    LOVE_SPOT_PHOTO_LIKE,
    LOVE,
    WISHLIST_ITEM,
    LOVER;

    companion object {
        fun ofType(type: NewsFeedItem.Type): NewsFeedItemType {
            return valueOf(type.name)
        }
    }
}

data class LoveSpotNewsFeedResponse(
    val id: Long,
    val createdAt: Instant,
    val addedBy: Long,
    val name: String,
    val description: String,
    val type: LoveSpot.Type,
    val country: String?
)

data class LoveNewsFeedResponse(
    val id: Long,
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val happenedAt: Instant,
    val loverPartnerId: Long?,
)

data class LoveSpotReviewNewsFeedResponse(
    val id: Long,
    val loveSpotId: Long,
    val reviewerId: Long,
    val submittedAt: Instant,
    val reviewText: String,
    val reviewStars: Int,
    val riskLevel: Int
)

data class LoveSpotPhotoNewsFeedResponse(
    val id: Long,
    val loveSpotId: Long,
    val uploadedBy: Long,
    val uploadedAt: Instant,
    val fileName: String,
    val loveSpotReviewId: Long?,
    val likes: Int,
    val dislikes: Int,
)

data class PhotoLikeNewsFeedResponse(
    val id: Long,
    val loveSpotId: Long,
    val loveSpotPhotoId: Long,
    val happenedAt: Instant,
    val loverId: Long,
    val likeOrDislike: Int
)

data class WishlistNewsFeedResponse(
    val id: Long,
    val loverId: Long,
    val loveSpotId: Long,
    val addedAt: Instant
)

data class LoverNewsFeedResponse(
    val id: Long,
    val userName: String,
    val publicProfile: Boolean,
    val joinedAt: Instant,
    val rank: Int,
    val points: Int,
    val uuid: String?
)
