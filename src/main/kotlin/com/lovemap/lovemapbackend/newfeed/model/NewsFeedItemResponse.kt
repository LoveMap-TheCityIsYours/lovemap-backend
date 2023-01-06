package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import java.time.Instant

data class NewsFeedItemResponse(
    val type: NewsFeedItemType,
    val generatedAt: Instant,
    val generatedAtFormatted: String,
    val happenedAt: Instant,
    val happenedAtFormatted: String,
    val referenceId: Long,
    val loveSpot: LoveSpotNewsFeedResponse? = null,
    val love: LoveNewsFeedResponse? = null,
)

enum class NewsFeedItemType {
    LOVE_SPOT,
    LOVE_SPOT_REVIEW,
    LOVE_SPOT_PHOTO,
    LOVE_SPOT_PHOTO_LIKE_DISLIKE,
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
