package com.lovemap.lovemapbackend.newsfeed

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem.Type.LOVE_SPOT
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemConverter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class LoveSpotNewsFeedUpdater(
    private val newsFeedItemConverter: NewsFeedItemConverter,
    private val newsFeedService: NewsFeedService,
    private val newsFeedRepository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun updateLoveSpotCountry(loveSpotId: Long, country: String) {
        try {
            newsFeedRepository.findByTypeAndReferenceId(LOVE_SPOT, loveSpotId)?.let { loveSpotItem ->
                loveSpotItem.country = country
                newsFeedRepository.save(loveSpotItem)
                logger.info { "Updating Country for LoveSpot NewsfeedItem '$loveSpotItem'" }
                newsFeedRepository.findAllAfterGeneratedAt(loveSpotItem.generatedAt)
                    .map { newsFeedItemConverter.dtoFromItem(it) }
                    .toList().forEach { newsFeedDto ->
                        if (newsFeedDto.newsFeedData.loveSpotId() == loveSpotId) {
                            newsFeedDto.id?.let { newsFeedRepository.findById(newsFeedDto.id) }
                                ?.let { newsFeedItem ->
                                    logger.info { "Updating Country '$newsFeedItem'" }
                                    newsFeedItem.country = country
                                    newsFeedRepository.save(newsFeedItem)
                                }
                        }
                    }
                newsFeedService.reloadCache()
            }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during updating country for NewsFeedItems" }
        }
    }
}