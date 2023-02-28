package com.lovemap.lovemapbackend.newsfeed.data

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.newsfeed.dataparser.NewsFeedDataParser
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import org.springframework.stereotype.Component

@Component
class NewsFeedItemConverter(
    private val newsFeedDataParser: NewsFeedDataParser,
    private val cachedLoverService: CachedLoverService,
) {

    suspend fun dtoFromItem(item: NewsFeedItem): NewsFeedItemDto {
        return NewsFeedItemDto(
            id = item.id,
            type = NewsFeedItemDto.Type.of(item.type),
            generatedAt = item.generatedAt.toInstant(),
            referenceId = item.referenceId,
            publicLover = cachedLoverService.getIfProfileIsPublic(item.loverId),
            country = item.country,
            newsFeedData = newsFeedDataParser.parse(item.type, item.data)
        )
    }

    suspend fun processedDtoFromItem(item: NewsFeedItem): ProcessedNewsFeedItemDto {
        val newsFeedItemDto = NewsFeedItemDto(
            id = item.id,
            type = NewsFeedItemDto.Type.of(item.type),
            generatedAt = item.generatedAt.toInstant(),
            referenceId = item.referenceId,
            publicLover = cachedLoverService.getIfProfileIsPublic(item.loverId),
            country = item.country,
            newsFeedData = newsFeedDataParser.parse(item.type, item.data)
        )
        return ProcessedNewsFeedItemDto.of(newsFeedItemDto)
    }
}