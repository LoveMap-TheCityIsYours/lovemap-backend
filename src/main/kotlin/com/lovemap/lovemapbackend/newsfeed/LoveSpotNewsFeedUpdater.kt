package com.lovemap.lovemapbackend.newsfeed

import com.fasterxml.jackson.databind.ObjectMapper
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
    private val objectMapper: ObjectMapper,
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
                            val dtoUpdate = newsFeedDto.copy(country = country)
                            logger.info { "Updating Country '$dtoUpdate'" }
                            newsFeedRepository.save(dtoUpdate.toNewsFeedItem(objectMapper))
                        }
                    }
                newsFeedService.reloadCache()
            }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during updating country for NewsFeedItems" }
        }
    }
}