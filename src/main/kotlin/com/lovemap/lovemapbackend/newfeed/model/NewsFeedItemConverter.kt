package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.dataparser.NewsFeedDataParser
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class NewsFeedItemConverter(
    private val newsFeedDataParser: NewsFeedDataParser,
    private val loveSpotNewsFeedResponseConverter: LoveSpotNewsFeedResponseConverter,
    private val loveNewsFeedResponseConverter: LoveNewsFeedResponseConverter,
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        .withZone(ZoneId.from(ZoneOffset.UTC))

    fun dtoFromItem(item: NewsFeedItem): NewsFeedItemDto {
        return NewsFeedItemDto(
            id = item.id,
            type = item.type,
            generatedAt = item.generatedAt.toInstant(),
            referenceId = item.referenceId,
            newsFeedData = newsFeedDataParser.parse(item.type, item.data)
        )
    }

    fun dtoToResponse(dto: NewsFeedItemDto): NewsFeedItemResponse {
        return NewsFeedItemResponse(
            type = NewsFeedItemType.ofType(dto.type),
            generatedAt = dto.generatedAt,
            generatedAtFormatted = dateTimeFormatter.format(dto.generatedAt),
            happenedAt = dto.newsFeedData.happenedAt(),
            happenedAtFormatted = dateTimeFormatter.format(dto.newsFeedData.happenedAt()),
            referenceId = dto.referenceId,
            loveSpot = loveSpotNewsFeedResponseConverter.convert(dto),
            love = loveNewsFeedResponseConverter.convert(dto),
        )
    }

}
