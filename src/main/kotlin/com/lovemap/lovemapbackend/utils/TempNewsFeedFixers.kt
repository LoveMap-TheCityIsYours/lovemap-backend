package com.lovemap.lovemapbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.lover.LoverRepository
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.newsfeed.LoverNewsFeedUpdater
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.dataparser.NewsFeedDataParser
import com.lovemap.lovemapbackend.newsfeed.model.*
import com.lovemap.lovemapbackend.lovespot.CachedLoveSpotService
import com.lovemap.lovemapbackend.newsfeed.provider.LoveSpotReviewNewsFeedProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import java.time.Duration
import java.time.Instant

// disabled
class NewsFeedCountryPopulator(
    private val newsFeedRepository: NewsFeedRepository,
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val cachedLoveSpotService: CachedLoveSpotService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        mono {
            newsFeedRepository.findAll().collect { newsFeedItem ->
                val newsFeedDto: NewsFeedItemDto = newsFeedItemConverter.dtoFromItem(newsFeedItem)
                val country: String = newsFeedDto.newsFeedData.loveSpotId()
                    ?.let { cachedLoveSpotService.getCountryByLoveSpotId(it) }
                    ?: NewsFeedItem.DEFAULT_COUNTRY
                newsFeedItem.country = country
                newsFeedRepository.save(newsFeedItem)
            }

        }.subscribe()
    }
}

// disabled
class NewsFeedPhotoFixer(
    private val newsFeedRepository: NewsFeedRepository,
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val objectMapper: ObjectMapper,
    private val photoService: LoveSpotPhotoService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        mono {
            newsFeedRepository.findAllByType(NewsFeedItem.Type.LOVE_SPOT_PHOTO).collect { newsFeedItem ->
                val newsFeedDto = newsFeedItemConverter.dtoFromItem(newsFeedItem)
                val photoData = newsFeedDto.newsFeedData as LoveSpotPhotoNewsFeedData
                photoService.getCachedPhoto(photoData.id)?.let { photo ->
                    val fixedPhotoData = photoData.copy(url = photo.url)
                    newsFeedItem.data = objectMapper.writeValueAsString(fixedPhotoData)
                    newsFeedRepository.save(newsFeedItem)
                }
            }

            newsFeedRepository.findAllByType(NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE).collect { newsFeedItem ->
                val newsFeedDto = newsFeedItemConverter.dtoFromItem(newsFeedItem)
                val likeData = newsFeedDto.newsFeedData as PhotoLikeNewsFeedData
                photoService.getCachedPhoto(likeData.loveSpotPhotoId)?.let { photo ->
                    val fixedLikeData: PhotoLikeNewsFeedData = likeData.copy(url = photo.url)
                    newsFeedItem.data = objectMapper.writeValueAsString(fixedLikeData)
                    newsFeedRepository.save(newsFeedItem)
                }
            }

        }.subscribe()
    }
}

// disabled
class NewsFeedReviewFixer(
    private val loveSpotReviewNewsFeedProvider: LoveSpotReviewNewsFeedProvider,
    private val newsFeedRepository: NewsFeedRepository,
    private val objectMapper: ObjectMapper,
) : ApplicationRunner {
    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        mono {
            val newsFeed: List<NewsFeedItemDto> = loveSpotReviewNewsFeedProvider.getNewsFeedFrom(
                Instant.now().minusSeconds(3600),
                Instant.now().minus(Duration.ofDays(3000))
            ).toList()
            logger.info { "Collected LoveSpotReviewNewsFeed size: ${newsFeed.size}" }
            val saved = newsFeed.map { it.toNewsFeedItem(objectMapper) }
                .map {
                    runCatching {
                        logger.info { "Saving $it" }
                        newsFeedRepository.save(it)
                    }.getOrNull()
                }
                .toList()
                .filterNotNull()
            logger.info { "Saved LoveSpotReviewNewsFeed size: ${saved.size}" }

        }.subscribe()
    }
}

// disabled
class LoverDisplayNameUpdater(
    private val loverRepository: LoverRepository,
    private val loverNewsFeedUpdater: LoverNewsFeedUpdater
) : ApplicationRunner {
    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        mono {
            val loversToUpdate = loverRepository.findAll()
                .filter { it.displayName == it.email }
                .toList()
            loversToUpdate.forEach {
                it.displayName = it.email.substringBefore("@")
                logger.info { "Updating NewsFeedItem for ${it.displayName}" }
                loverNewsFeedUpdater.updateLoverNameChange(it.id, it.displayName)
                val saved = loverRepository.save(it)
                logger.info { "Saved $saved" }
            }

        }.subscribe()
    }
}

// disabled
class LoverCountryNameUpdater(
    private val loverRepository: LoverRepository,
    private val newsFeedRepository: NewsFeedRepository,
) : ApplicationRunner {
    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        mono {
            val loverNewsFeedItems = newsFeedRepository.findAllByType(NewsFeedItem.Type.LOVER).toList()
            loverNewsFeedItems.forEach { newsFeedItem ->
                loverRepository.findById(newsFeedItem.referenceId)?.let { lover ->
                    if (newsFeedItem.country != lover.registrationCountry) {
                        newsFeedItem.country = lover.registrationCountry
                        val saved = newsFeedRepository.save(newsFeedItem)
                        logger.info { "Saved lover country '${lover.registrationCountry}' for loverNewsFeedItem '$saved'" }
                    }
                }
            }

        }.subscribe()
    }
}

// disabled
class NewsFeedItemLoverIdFixer(
    private val newsFeedRepository: NewsFeedRepository,
    private val newsFeedDataParser: NewsFeedDataParser,
) : ApplicationRunner {
    private val logger = KotlinLogging.logger { }

    override fun run(args: ApplicationArguments?) {
        mono {
            newsFeedRepository.findAll().map { newsFeedItem: NewsFeedItem ->
                val newsFeedData: NewsFeedData = newsFeedDataParser.parse(newsFeedItem.type, newsFeedItem.data)
                newsFeedItem.loverId = newsFeedData.loverId()
                val saved = newsFeedRepository.save(newsFeedItem)
                logger.info { "Set loverId '${saved.loverId}' for NewsFeedItem $saved" }
                saved
            }.collect()

        }.subscribe()
    }
}












