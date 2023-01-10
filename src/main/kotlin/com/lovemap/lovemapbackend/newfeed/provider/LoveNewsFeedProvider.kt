package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoveNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoveNewsFeedProvider(
    private val loveService: LoveService,
    private val cachedLoveSpotService: CachedLoveSpotService
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for Loves from $generateFrom" }
        val loves = loveService.getLovesFrom(generateFrom)
        return loves.map {
            NewsFeedItemDto(
                type = NewsFeedItem.Type.LOVE,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = loveToNewsFeedData(it),
                country = cachedLoveSpotService.getCountryByLoveSpotId(it.loveSpotId)
            )
        }
    }

    private suspend fun loveToNewsFeedData(love: Love): LoveNewsFeedData {
        return LoveNewsFeedData(
            id = love.id,
            name = love.name,
            loveSpotId = love.loveSpotId,
            loverId = love.loverId,
            happenedAt = love.happenedAt.toInstant(),
            loverPartnerId = love.loverPartnerId
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE
    }
}
