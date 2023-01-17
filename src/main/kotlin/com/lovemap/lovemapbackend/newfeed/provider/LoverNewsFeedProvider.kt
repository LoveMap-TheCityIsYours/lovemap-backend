package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lover.Lover
import com.lovemap.lovemapbackend.lover.LoverService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoverNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoverNewsFeedProvider(
    private val loverService: LoverService
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger{}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for Lovers from $generateFrom" }
        val lovers = loverService.getLoversFrom(generateFrom)
        return lovers.map {
            NewsFeedItemDto(
                type = NewsFeedItem.Type.LOVER,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = loverToNewsFeedData(it),
                country = it.registrationCountry
            )
        }
    }

    private suspend fun loverToNewsFeedData(lover: Lover): LoverNewsFeedData {
        return LoverNewsFeedData(
            id = lover.id,
            joinedAt = lover.createdAt.toInstant(),
            userName = lover.displayName,
            publicProfile = false,
            rank = lover.rank,
            points = lover.points,
            uuid = lover.uuid
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVER
    }
}
