package com.lovemap.lovemapbackend.lover

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
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

    private val loverUserNameCache: Cache<String, Lover> = CacheBuilder.newBuilder()
        .initialCapacity(200)
        .concurrencyLevel(max(Runtime.getRuntime().availableProcessors() / 2, 2))
        .maximumSize(1000)
        .build()

    suspend fun getCachedLoverById(loverId: Long): LoverViewWithoutRelationResponse? {
        logger.info { "Getting Lover from Cache '$loverId'." }
        return loverCache.getIfPresent(loverId)?.let { lover ->
            logger.info { "Lover found in Cache '$loverId'." }
            LoverViewWithoutRelationResponse.of(lover)
        } ?: run {
            logger.info { "Lover not found in Cache '$loverId'. Getting from DB." }
            val lover = loverRepository.findById(loverId)
            lover?.let {
                logger.info { "Lover found in DB '$loverId'. Inserting into Cache." }
                loverCache.put(loverId, lover)
                loverUserNameCache.put(lover.userName, lover)
            } ?: run {
                logger.info { "Lover not found in DB '$loverId'. Returning null." }
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
        loverUserNameCache.put(lover.userName, lover)
        logger.info { "Lover was put into the Cache '${lover.id}'." }
    }

    suspend fun getByUserName(userName: String): Lover {
        return loverUserNameCache.getIfPresent(userName)
            ?: loverRepository.findByUserName(userName)?.also { lover ->
                loverUserNameCache.put(userName, lover)
                loverCache.put(lover.id, lover)
            } ?: throw LoveMapException(HttpStatus.NOT_FOUND, ErrorCode.LoverNotFound)
    }
}