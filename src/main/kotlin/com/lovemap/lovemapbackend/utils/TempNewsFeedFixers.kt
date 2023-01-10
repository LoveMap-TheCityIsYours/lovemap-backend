package com.lovemap.lovemapbackend.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedRepository
import com.lovemap.lovemapbackend.newfeed.model.LoveSpotPhotoNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemConverter
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto
import com.lovemap.lovemapbackend.newfeed.model.PhotoLikeNewsFeedData
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

            newsFeedRepository.findAllByType(NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE).collect {newsFeedItem ->
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
