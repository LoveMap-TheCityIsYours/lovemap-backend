package com.lovemap.lovemapbackend.newsfeed.model

import com.lovemap.lovemapbackend.lover.CachedLoverService
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.dataparser.NewsFeedDataParser
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemType
import com.lovemap.lovemapbackend.newsfeed.model.response.decorators.NewsFeedDataResponseDecorator
import com.lovemap.lovemapbackend.newsfeed.model.response.decorators.ProcessedNewsFeedDataResponseDecorator
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
    responseDecorators: List<NewsFeedDataResponseDecorator>,
    processedResponseDecorators: List<ProcessedNewsFeedDataResponseDecorator>
) {
    private val newsFeedDataDecoratorMap: Map<NewsFeedItemDto.Type, NewsFeedDataResponseDecorator> =
        responseDecorators.associateBy { it.supportedType() }

    private val processedNewsFeedDataDecoratorMap:
            Map<ProcessedNewsFeedItemDto.ProcessedType, ProcessedNewsFeedDataResponseDecorator> =
        processedResponseDecorators.associateBy { it.supportedType() }

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        .withZone(ZoneId.from(ZoneOffset.UTC))

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

    suspend fun dtoToResponse(dto: NewsFeedItemDto): NewsFeedItemResponse {
        val type = when (dto) {
            is ProcessedNewsFeedItemDto -> NewsFeedItemType.ofType(dto.processedType)
            else -> NewsFeedItemType.ofType(dto.type)
        }

        val initializedResponse = NewsFeedItemResponse(
            type = type,
            generatedAt = dto.generatedAt,
            generatedAtFormatted = dateTimeFormatter.format(dto.generatedAt),
            happenedAt = dto.newsFeedData.happenedAt(),
            happenedAtFormatted = dateTimeFormatter.format(dto.newsFeedData.happenedAt()),
            referenceId = dto.referenceId,
            loverId = dto.newsFeedData.loverId(),
            publicLover = dto.publicLover,
            country = dto.country
        )

        return when (dto) {

            is ProcessedNewsFeedItemDto ->
                processedNewsFeedDataDecoratorMap[dto.processedType]
                    ?.decorate(initializedResponse, dto.processedData)

            else -> newsFeedDataDecoratorMap[dto.type]?.decorate(initializedResponse, dto.newsFeedData)

        } ?: throw LoveMapException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.InternalServerError)
    }

}
