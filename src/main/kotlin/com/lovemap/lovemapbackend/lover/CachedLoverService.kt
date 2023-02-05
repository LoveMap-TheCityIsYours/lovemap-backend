package com.lovemap.lovemapbackend.lover

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.math.max

@Service
class CachedLoverService(
    private val loverRepository: LoverRepository
) {
    private val logger = KotlinLogging.logger {}

    private val loverCache: Cache<Long, Lover> = CacheBuilder.newBuilder()
        .initialCapacity(200)
        .expireAfterWrite(Duration.ofMinutes(60))
        .concurrencyLevel(max(Runtime.getRuntime().availableProcessors() / 2, 2))
        .maximumSize(1000)
        .build()

    suspend fun getCachedLoverById(loverId: Long): LoverViewWithoutRelationResponse? {
        logger.debug { "Getting Lover from Cache '$loverId'." }
        return loverCache.getIfPresent(loverId)?.let { lover ->
            logger.debug { "Lover found in Cache '$loverId'." }
            LoverViewWithoutRelationResponse.of(lover)
        } ?: run {
            logger.debug { "Lover not found in Cache '$loverId'. Getting from DB." }
            val lover = loverRepository.findById(loverId)
            lover?.let {
                logger.debug { "Lover found in DB '$loverId'. Inserting into Cache." }
                loverCache.put(loverId, lover)
            } ?: run {
                logger.debug { "Lover not found in DB '$loverId'. Returning null." }
            }
            lover
        }?.let {
            LoverViewWithoutRelationResponse.of(it)
        }
    }

    suspend fun getIfProfileIsPublic(loverId: Long): LoverViewWithoutRelationResponse? {
        return getCachedLoverById(loverId)?.takeIf { it.publicProfile }
    }

    fun put(lover: Lover) {
        loverCache.put(lover.id, lover)
        logger.debug { "Lover was put into the Cache '${lover.id}'." }
    }
}