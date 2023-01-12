package com.lovemap.lovemapbackend.newfeed

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem.Type.LOVER
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.dataparser.LoverNewsFeedDataParser
import com.lovemap.lovemapbackend.newfeed.model.LoverNewsFeedData
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class LoverNewsFeedUpdater(
    private val loverNewsFeedDataParser: LoverNewsFeedDataParser,
    private val objectMapper: ObjectMapper,
    private val newsFeedRepository: NewsFeedRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun updateLoverNameChange(loverId: Long, displayName: String) {
        try {
            newsFeedRepository.findByTypeAndReferenceId(LOVER, loverId)?.let { newsFeedItem ->
                logger.info { "Found NewsFeedItem for '$displayName' and updating it." }
                val loverNewsFeedData = loverNewsFeedDataParser.parse(newsFeedItem.data) as LoverNewsFeedData
                val updatedData = loverNewsFeedData.copy(userName = displayName)
                newsFeedItem.data = objectMapper.writeValueAsString(updatedData)
                val saved = newsFeedRepository.save(newsFeedItem)
                logger.info { "Saved '$saved'" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error occurred during updating NewsFeedItem for Lover DisplayName change" }
        }
    }
}