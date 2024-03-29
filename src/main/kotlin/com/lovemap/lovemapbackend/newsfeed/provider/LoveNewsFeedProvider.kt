package com.lovemap.lovemapbackend.newsfeed.provider

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.LoveNewsFeedData
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class LoveNewsFeedProvider(
    private val loveService: LoveService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val cachedLoverService: CachedLoverService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        val modifiedGenerateFrom = generateFrom.minus(Duration.ofMinutes(15))
        logger.info { "Getting NewsFeed for Loves from $modifiedGenerateFrom" }
        val loves = loveService.getLovesFrom(modifiedGenerateFrom)
        return loves.map {
            NewsFeedItemDto(
                type = NewsFeedItemDto.Type.LOVE,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = loveToNewsFeedData(it),
                publicLover = cachedLoverService.getIfProfileIsPublic(it.loverId),
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
            loverPartnerId = love.loverPartnerId,
            publicLoverPartner = love.loverPartnerId?.let { cachedLoverService.getIfProfileIsPublic(it) }
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE
    }
}
