package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.lover.LoverViewWithoutRelationResponse
import com.lovemap.lovemapbackend.newsfeed.data.*
import java.time.Instant
import java.util.*
import kotlin.collections.Collection

data class ProcessedNewsFeedItemDto(
    val delegate: NewsFeedItemDto,
    val processedType: ProcessedType,
    val processedData: ComparableNewsFeedData,
    val origins: List<NewsFeedItemDto> = emptyList(),
) : NewsFeedItemDto(
    delegate.id,
    delegate.type,
    delegate.generatedAt,
    delegate.publicLover,
    delegate.country,
    delegate.referenceId,
    delegate.newsFeedData
) {
    companion object {
        fun of(newsFeedItemDto: NewsFeedItemDto): ProcessedNewsFeedItemDto {
            return ProcessedNewsFeedItemDto(
                delegate = newsFeedItemDto,
                processedType = ProcessedType.of(newsFeedItemDto.type),
                processedData = newsFeedItemDto.newsFeedData,
                origins = listOf(newsFeedItemDto)
            )
        }
    }

    enum class ProcessedType {
        LOVE_SPOT,
        LOVE_SPOT_REVIEW,
        LOVE_SPOT_PHOTO,
        LOVE_SPOT_PHOTO_LIKE,
        LOVE,
        WISHLIST_ITEM,
        LOVER,

        MULTI_LOVER,
        PRIVATE_LOVERS,
        LOVE_SPOT_MULTI_EVENTS,
        LOVE_SPOT_MULTI_PHOTOS;

        companion object {
            fun of(type: Type): ProcessedType {
                return valueOf(type.name)
            }
        }
    }
}

abstract class ProcessedNewsFeedData : ComparableNewsFeedData()

data class MultiLoverNewsFeedData(
    val lovers: TreeSet<LoverNewsFeedData>
) : ProcessedNewsFeedData() {
    override fun happenedAt(): Instant = lovers.first().joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = lovers.first().id
}

data class PrivateLoversNewsFeedData(
    val lovers: TreeSet<LoverNewsFeedData>
) : ProcessedNewsFeedData() {
    override fun happenedAt(): Instant = lovers.first().joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = lovers.first().id
}

data class LoveSpotMultiEventsNewsFeedData(
    val loveSpot: LoveSpotNewsFeedData,
    val lovers: MutableList<LoverViewWithoutRelationResponse> = ArrayList(),
    val loves: MutableList<LoveNewsFeedData> = ArrayList(),
    val reviews: MutableList<LoveSpotReviewNewsFeedData> = ArrayList(),
    val photos: MutableList<LoveSpotPhotoNewsFeedData> = ArrayList(),
) : ProcessedNewsFeedData() {
    var loveSpotAddedHere: Boolean = false
        private set
    private var happenedAt: Instant = Instant.MIN

    override fun happenedAt(): Instant = happenedAt
    override fun loveSpotId(): Long = loveSpot.id
    override fun loverId(): Long = loveSpot.addedBy

    fun addProcessedData(processedData: ComparableNewsFeedData): LoveSpotMultiEventsNewsFeedData {
        when (processedData) {
            is LoveNewsFeedData -> addLove(processedData)
            is LoveSpotReviewNewsFeedData -> addReview(processedData)
            is LoveSpotPhotoNewsFeedData -> addPhoto(processedData)
            is LoveSpotNewsFeedData -> loveSpotAddedHere = true
        }
        return this
    }

    private fun addLove(love: LoveNewsFeedData) {
        if (love.happenedAt.isAfter(happenedAt)) {
            happenedAt = love.happenedAt
        }
        loves.add(love)
    }

    private fun addReview(review: LoveSpotReviewNewsFeedData) {
        if (review.submittedAt.isAfter(happenedAt)) {
            happenedAt = review.submittedAt
        }
        reviews.add(review)
    }

    private fun addPhoto(photo: LoveSpotPhotoNewsFeedData) {
        if (photo.uploadedAt.isAfter(happenedAt)) {
            happenedAt = photo.uploadedAt
        }
        photos.add(photo)
    }

    fun getLoverIds(): Set<Long> {
        return setOf(loveSpot.addedBy) +
                loves.map { it.loverId } +
                reviews.map { it.reviewerId } +
                photos.map { it.uploadedBy }
    }

    fun addLovers(lover: Collection<LoverViewWithoutRelationResponse>) {
        lovers.addAll(lover)
    }
}
