package com.lovemap.lovemapbackend.newsfeed.model

import java.time.Instant
import java.util.*

data class ProcessedNewsFeedItemDto(
    val delegate: NewsFeedItemDto,
    val processedType: ProcessedType,
    val processedData: ProcessedNewsFeedData,
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
    enum class ProcessedType {
        MULTI_LOVER,
        PRIVATE_LOVERS
    }
}

interface ProcessedNewsFeedData : NewsFeedData

data class MultiLoverNewsFeedData(
    val lovers: TreeSet<LoverNewsFeedData>
) : ProcessedNewsFeedData {
    override fun happenedAt(): Instant = lovers.first().joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = lovers.first().id
}

data class PrivateLoversNewsFeedData(
    val lovers: TreeSet<LoverNewsFeedData>
) : ProcessedNewsFeedData {
    override fun happenedAt(): Instant = lovers.first().joinedAt
    override fun loveSpotId(): Long? = null
    override fun loverId(): Long = lovers.first().id
}