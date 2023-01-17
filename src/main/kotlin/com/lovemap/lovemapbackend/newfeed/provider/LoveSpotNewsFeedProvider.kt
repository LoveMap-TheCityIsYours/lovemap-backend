package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoveSpotNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoveSpotNewsFeedProvider(
    private val loveSpotService: LoveSpotService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val cachedLoverService: CachedLoverService,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override suspend fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for LoveSpots from $generateFrom" }
        val loveSpots = loveSpotService.getLoveSpotsFrom(generateFrom)
        return loveSpots.map {
            val loveSpotNewsFeedData: LoveSpotNewsFeedData = loveSpotToNewsFeedData(it)
            NewsFeedItemDto(
                type = NewsFeedItem.Type.LOVE_SPOT,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = loveSpotNewsFeedData,
                publicLover = cachedLoverService.getIfProfileIsPublic(it.addedBy),
                country = loveSpotNewsFeedData.country ?: NewsFeedItem.DEFAULT_COUNTRY
            )
        }
    }

    private suspend fun loveSpotToNewsFeedData(loveSpot: LoveSpot): LoveSpotNewsFeedData {
        return LoveSpotNewsFeedData(
            id = loveSpot.id,
            createdAt = loveSpot.createdAt.toInstant(),
            addedBy = loveSpot.addedBy,
            name = loveSpot.name,
            description = loveSpot.description,
            type = loveSpot.type,
            country = cachedLoveSpotService.getCountryByLoveSpotId(loveSpot.id),
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT
    }
}
