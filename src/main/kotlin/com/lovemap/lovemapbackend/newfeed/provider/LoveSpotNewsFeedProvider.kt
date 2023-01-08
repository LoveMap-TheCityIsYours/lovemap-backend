package com.lovemap.lovemapbackend.newfeed.provider

import com.lovemap.lovemapbackend.geolocation.CachedGeoLocationProvider
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
    private val geoLocationProvider: CachedGeoLocationProvider,
) : NewsFeedProvider {
    private val logger = KotlinLogging.logger {}

    override fun getNewsFeedFrom(generationTime: Instant, generateFrom: Instant): Flow<NewsFeedItemDto> {
        logger.info { "Getting NewsFeed for LoveSpots from $generateFrom" }
        val loveSpots = loveSpotService.getLoveSpotsFrom(generateFrom)
        return loveSpots.map {
            NewsFeedItemDto(
                type = NewsFeedItem.Type.LOVE_SPOT,
                generatedAt = generationTime,
                referenceId = it.id,
                newsFeedData = loveSpotToNewsFeedData(it)
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
            country = loveSpot.geoLocationId?.let { geoLocationProvider.findCountryByGeoLocationId(it) },
        )
    }

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT
    }
}
