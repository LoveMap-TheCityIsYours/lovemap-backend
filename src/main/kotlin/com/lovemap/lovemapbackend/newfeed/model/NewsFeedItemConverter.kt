package com.lovemap.lovemapbackend.newfeed.model

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
    responseDecorators: List<TypeBasedNewsFeedResponseDecorator>
) {
    private val responseDecoratorMap: Map<NewsFeedItem.Type, TypeBasedNewsFeedResponseDecorator> =
        responseDecorators.associateBy { it.supportedType() }

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
        val initializedResponse = NewsFeedItemResponse(
            type = NewsFeedItemType.ofType(dto.type),
            generatedAt = dto.generatedAt,
            generatedAtFormatted = dateTimeFormatter.format(dto.generatedAt),
            happenedAt = dto.newsFeedData.happenedAt(),
            happenedAtFormatted = dateTimeFormatter.format(dto.newsFeedData.happenedAt()),
            referenceId = dto.referenceId
        )
        return responseDecoratorMap[dto.type]?.decorate(initializedResponse, dto.newsFeedData)
            ?: throw LoveMapException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.InternalServerError)
    }

}
