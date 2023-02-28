package com.lovemap.lovemapbackend.newsfeed.api

import com.lovemap.lovemapbackend.newsfeed.api.decorators.NewsFeedDataResponseDecorator
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto
import com.lovemap.lovemapbackend.newsfeed.processor.ProcessedNewsFeedItemDto.ProcessedType
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class NewsFeedResponseConverter(
    responseDecorators: List<NewsFeedDataResponseDecorator>,
) {
    private val newsFeedDataDecoratorMap: Map<ProcessedType, NewsFeedDataResponseDecorator> =
        responseDecorators.associateBy { it.supportedType() }

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        .withZone(ZoneId.from(ZoneOffset.UTC))

    suspend fun processedDtoToResponse(dto: ProcessedNewsFeedItemDto): NewsFeedItemResponse {
        val initializedResponse = NewsFeedItemResponse(
            type = NewsFeedItemType.ofType(dto.processedType),
            generatedAt = dto.generatedAt,
            generatedAtFormatted = dateTimeFormatter.format(dto.generatedAt),
            happenedAt = dto.newsFeedData.happenedAt(),
            happenedAtFormatted = dateTimeFormatter.format(dto.newsFeedData.happenedAt()),
            referenceId = dto.referenceId,
            loverId = dto.newsFeedData.loverId(),
            publicLover = dto.publicLover,
            country = dto.country
        )

        return newsFeedDataDecoratorMap[dto.processedType]?.decorate(initializedResponse, dto.processedData)
            ?: throw LoveMapException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.InternalServerError)
    }

}
