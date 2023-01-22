package com.lovemap.lovemapbackend.lovespot

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class CachedLoveSpotService(
    private val loveSpotRepository: LoveSpotRepository,
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

    suspend fun put(loveSpot: LoveSpot) {
        loveSpotCache.put(loveSpot.id, loveSpot)
        logger.info { "LoveSpot was put into the cache '${loveSpot.id}'" }
        val country = getCountryByLoveSpotId(loveSpot.id)
        logger.info { "Country was put into the cache '$country'." }
    }

    suspend fun findById(loveSpotId: Long): LoveSpot? {
        logger.info { "Getting LoveSpot from Cache '$loveSpotId'." }
        return loveSpotCache.getIfPresent(loveSpotId)?.let { loveSpot ->
            logger.info { "LoveSpot found in Cache '$loveSpotId'." }
            loveSpot
        } ?: run {
            logger.info { "LoveSpot not found in Cache '$loveSpotId'. Getting from DB." }
            val loveSpot = loveSpotRepository.findById(loveSpotId)
            loveSpot?.let {
                logger.info { "LoveSpot found in DB '$loveSpotId'. Inserting into Cache." }
                loveSpotCache.put(loveSpotId, loveSpot)
            } ?: run {
                logger.info { "LoveSpot not found in DB '$loveSpotId'. Returning null." }
            }
            loveSpot
        }
    }

    suspend fun getCountryByLoveSpotId(loveSpotId: Long): String {
        logger.info { "Getting Country for LoveSpot from Cache '$loveSpotId'." }
        val cachedCountry = loveSpotCountryCache.getIfPresent(loveSpotId)
        return if (cachedCountry == null) {
            logger.info { "Country for LoveSpot not found in Cache '$loveSpotId'. Getting from DB." }
            getCountryFromGeoLocationDb(loveSpotId)
        } else {
            logger.info { "Found Country for LoveSpot in Cache: '$cachedCountry', '$loveSpotId'." }
            if (cachedCountry == GeoLocation.GLOBAL_LOCATION) {
                getCountryFromGeoLocationDb(loveSpotId)
            } else {
                cachedCountry
            }

        }
    }

    private suspend fun getCountryFromGeoLocationDb(loveSpotId: Long): String {
        return findById(loveSpotId)?.geoLocationId
            ?.let { geoLocationService.findGeoLocationById(it) }
            ?.country
            ?.let {
                logger.info { "Resolved Country for LoveSpot from DB: '$it', '$loveSpotId'. Inserting into Cache." }
                loveSpotCountryCache.put(loveSpotId, it)
                it
            }
            ?: NewsFeedItem.DEFAULT_COUNTRY
    }
}