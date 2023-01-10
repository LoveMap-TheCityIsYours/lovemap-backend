package com.lovemap.lovemapbackend.newfeed.provider

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class CachedLoveSpotService(
    private val loveSpotService: LoveSpotService,
    private val geoLocationService: GeoLocationService,
) {
    private val logger = KotlinLogging.logger {}

    private val loveSpotCache: Cache<Long, LoveSpot> = CacheBuilder.newBuilder()
        .initialCapacity(200)
        .concurrencyLevel(max(Runtime.getRuntime().availableProcessors() / 2, 2))
        .maximumSize(500)
        .build()

    private val loveSpotCountryCache: Cache<Long, String> = CacheBuilder.newBuilder()
        .initialCapacity(200)
        .concurrencyLevel(max(Runtime.getRuntime().availableProcessors() / 2, 2))
        .maximumSize(10000)
        .build()

    suspend fun findById(loveSpotId: Long): LoveSpot? {
        logger.info { "Getting LoveSpot '$loveSpotId' from Cache." }
        return loveSpotCache.getIfPresent(loveSpotId)?.let { loveSpot ->
            logger.info { "LoveSpot '$loveSpotId' found in Cache." }
            loveSpot
        } ?: runCatching {
            logger.info { "LoveSpot '$loveSpotId' not found in Cache. Getting from DB." }
            loveSpotService.getById(loveSpotId)
        }.onSuccess { loveSpot ->
            logger.info { "LoveSpot '$loveSpotId' found in DB. Inserting into Cache." }
            loveSpotCache.put(loveSpotId, loveSpot)
        }.onFailure {
            logger.info { "LoveSpot '$loveSpotId' not found in DB. Returning null." }
        }.getOrNull()
    }

    suspend fun getCountryByLoveSpotId(loveSpotId: Long): String {
        logger.info { "Getting Country for LoveSpot '$loveSpotId' from Cache." }
        val cachedCountry = loveSpotCountryCache.getIfPresent(loveSpotId)
        return if (cachedCountry == null) {
            logger.info { "Country for LoveSpot '$loveSpotId' not found in Cache. Getting from DB." }
            val country: String = findById(loveSpotId)?.geoLocationId
                ?.let { geoLocationService.findGeoLocationById(it) }?.country
                ?: NewsFeedItem.DEFAULT_COUNTRY
            logger.info { "Resolved '$country' Country for LoveSpot '$loveSpotId' from DB. Inserting into Cache." }
            loveSpotCountryCache.put(loveSpotId, country)
            country
        } else {
            logger.info { "Found '$cachedCountry' Country for LoveSpot '$loveSpotId' in Cache." }
            cachedCountry
        }
    }
}