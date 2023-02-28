package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.data.*
import java.time.Instant
import java.util.*

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
    val loveSpotEvents: TreeSet<ComparableNewsFeedData>
) : ProcessedNewsFeedData() {
    override fun happenedAt(): Instant = loveSpotEvents.first().happenedAt()
    override fun loveSpotId(): Long? = loveSpotEvents.first().loveSpotId()
    override fun loverId(): Long = loveSpotEvents.first().loverId()
}
