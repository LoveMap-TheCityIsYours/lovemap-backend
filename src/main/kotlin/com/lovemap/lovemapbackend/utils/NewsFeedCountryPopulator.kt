package com.lovemap.lovemapbackend.utils

import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.provider.CachedLoveSpotService
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

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