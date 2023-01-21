package com.lovemap.lovemapbackend.newfeed.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.lover.LoverViewWithoutRelationResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import java.sql.Timestamp
import java.time.Instant
import java.util.*

data class NewsFeedItemDto(
    val id: Long? = null,
    val type: Type,
    val generatedAt: Instant,
    val publicLover: LoverViewWithoutRelationResponse?,
    val country: String,
    val referenceId: Long,
    val newsFeedData: NewsFeedData
) : Comparable<NewsFeedItemDto> {

    enum class Type {
        LOVE_SPOT,
        LOVE_SPOT_REVIEW,
        LOVE_SPOT_PHOTO,
        LOVE_SPOT_PHOTO_LIKE,
        LOVE,
        WISHLIST_ITEM,
        LOVER,
        // these types are not stored in the DB, it's merged from Lovers by the NewsFeedProcessor
        MULTI_LOVER,
        PRIVATE_LOVERS;

        companion object {
            fun of(type: NewsFeedItem.Type): Type {
                return valueOf(type.name)
            }
        }

        fun toNewsFeedItemType(): NewsFeedItem.Type {
            return NewsFeedItem.Type.valueOf(name)
        }
    }

    override fun compareTo(other: NewsFeedItemDto): Int {
        val timeDiff = other.newsFeedData.happenedAt().compareTo(newsFeedData.happenedAt())
        if (timeDiff != 0) {
            return timeDiff
        }
        val typeDiff = other.type.compareTo(type)
        if (typeDiff != 0) {
            return typeDiff
        }
        return other.referenceId.compareTo(referenceId)
    }

    fun toNewsFeedItem(objectMapper: ObjectMapper): NewsFeedItem {
        return NewsFeedItem(
            generatedAt = Timestamp.from(generatedAt),
            happenedAt = Timestamp.from(newsFeedData.happenedAt()),
            type = type.toNewsFeedItemType(),
            referenceId = referenceId,
            loverId = newsFeedData.loverId(),
            country = country,
            data = objectMapper.writeValueAsString(newsFeedData)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NewsFeedItemDto

        if (type != other.type) return false
        if (newsFeedData.happenedAt() != other.newsFeedData.happenedAt()) return false
        if (referenceId != other.referenceId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + newsFeedData.happenedAt().hashCode()
        result = 31 * result + referenceId.hashCode()
        return result
    }
}

interface NewsFeedData {
    fun happenedAt(): Instant
    fun loveSpotId(): Long?
    fun loverId(): Long
}

data class LoveSpotNewsFeedData(
    val id: Long,
    val createdAt: Instant,
    val addedBy: Long,
    val name: String,
    val description: String,
    val type: LoveSpot.Type,
    val country: String?
) : NewsFeedData {
    override fun happenedAt(): Instant = createdAt
    override fun loveSpotId(): Long = id
    override fun loverId(): Long = addedBy
}

data class LoveSpotReviewNewsFeedData(
    val id: Long,
    val loveSpotId: Long,
    val reviewerId: Long,
    val submittedAt: Instant,
    val reviewText: String,
    val reviewStars: Int,
    val riskLevel: Int
) : NewsFeedData {
    override fun happenedAt(): Instant = submittedAt
    override fun loveSpotId(): Long = loveSpotId
    override fun loverId(): Long = reviewerId
}

data class LoveSpotPhotoNewsFeedData(
    val id: Long,
    val loveSpotId: Long,
    val uploadedBy: Long,
    val uploadedAt: Instant,
    val fileName: String,
    val url: String?,
    val loveSpotReviewId: Long?,
    val likes: Int,
    val dislikes: Int,
) : NewsFeedData {
    override fun happenedAt(): Instant = uploadedAt
    override fun loveSpotId(): Long = loveSpotId
    override fun loverId(): Long = uploadedBy
}

data class PhotoLikeNewsFeedData(
    val id: Long,
    val loveSpotId: Long,
    val loveSpotPhotoId: Long,
    val url: String?,
    val happenedAt: Instant,
    val loverId: Long,
    val likeOrDislike: Int
) : NewsFeedData {
    override fun happenedAt(): Instant = happenedAt
    override fun loveSpotId(): Long = loveSpotId
    override fun loverId(): Long = loverId
}

data class LoveNewsFeedData(
    val id: Long,
    val name: String,
    val loveSpotId: Long,
    val loverId: Long,
    val happenedAt: Instant,
    val loverPartnerId: Long?,
    val publicLoverPartner: LoverViewWithoutRelationResponse?
) : NewsFeedData {
    override fun happenedAt(): Instant = happenedAt
    override fun loveSpotId(): Long = loveSpotId
    override fun loverId(): Long = loverId
}

data class WishlistNewsFeedData(
    val id: Long,
    val loverId: Long,
    val loveSpotId: Long,
    val addedAt: Instant
) : NewsFeedData {
    override fun happenedAt(): Instant = addedAt
    override fun loveSpotId(): Long = loveSpotId
    override fun loverId(): Long = loverId
}

data class LoverNewsFeedData(
    val id: Long,
    val userName: String, // actually stores displayName
    val publicProfile: Boolean,
    val joinedAt: Instant,
    val rank: Int,
    val points: Int,
    val uuid: String?
) : NewsFeedData, Comparable<LoverNewsFeedData> {
    override fun happenedAt(): Instant = joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = id

    override fun compareTo(other: LoverNewsFeedData): Int {
        val timeDiff = other.joinedAt.compareTo(joinedAt)
        if (timeDiff != 0) {
            return timeDiff
        }
        return other.id.compareTo(id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoverNewsFeedData

        if (joinedAt != other.joinedAt) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = joinedAt.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}

data class MultiLoverNewsFeedData(
    val lovers: TreeSet<LoverNewsFeedData>
) : NewsFeedData {
    override fun happenedAt(): Instant = lovers.last().joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = lovers.last().id
}

data class PrivateLoversNewsFeedData(
    val lovers: TreeSet<LoverNewsFeedData>
) : NewsFeedData {
    override fun happenedAt(): Instant = lovers.last().joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = lovers.last().id
}
