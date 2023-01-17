package com.lovemap.lovemapbackend.newfeed.model

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.dataparser.NewsFeedDataParser
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class NewsFeedItemConverter(
    private val newsFeedDataParser: NewsFeedDataParser,
    private val cachedLoverService: CachedLoverService,
    responseDecorators: List<TypeBasedNewsFeedResponseDecorator>
) {
    private val responseDecoratorMap: Map<NewsFeedItem.Type, TypeBasedNewsFeedResponseDecorator> =
        responseDecorators.associateBy { it.supportedType() }

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        .withZone(ZoneId.from(ZoneOffset.UTC))

    suspend fun dtoFromItem(item: NewsFeedItem): NewsFeedItemDto {
        return NewsFeedItemDto(
            id = item.id,
            type = item.type,
            generatedAt = item.generatedAt.toInstant(),
            referenceId = item.referenceId,
            publicLover = cachedLoverService.getIfProfileIsPublic(item.loverId),
            country = item.country,
            newsFeedData = newsFeedDataParser.parse(item.type, item.data)
        )
    }

    suspend fun dtoToResponse(dto: NewsFeedItemDto): NewsFeedItemResponse {
        val initializedResponse = NewsFeedItemResponse(
            type = NewsFeedItemType.ofType(dto.type),
            generatedAt = dto.generatedAt,
            generatedAtFormatted = dateTimeFormatter.format(dto.generatedAt),
            happenedAt = dto.newsFeedData.happenedAt(),
            happenedAtFormatted = dateTimeFormatter.format(dto.newsFeedData.happenedAt()),
            referenceId = dto.referenceId,
            loverId = dto.newsFeedData.loverId(),
            publicLover = dto.publicLover,
            country = dto.country
        )
        return responseDecoratorMap[dto.type]?.decorate(initializedResponse, dto.newsFeedData)
            ?: throw LoveMapException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.InternalServerError)
    }

}
