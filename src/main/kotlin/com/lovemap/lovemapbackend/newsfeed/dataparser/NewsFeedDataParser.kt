package com.lovemap.lovemapbackend.newsfeed.dataparser

import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedData
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class NewsFeedDataParser(
    parsers: List<TypeBasedNewsFeedDataParser>
) {
    private val typeBasedParsers: Map<NewsFeedItem.Type, TypeBasedNewsFeedDataParser> = parsers.associateBy { it.supportedType() }

    fun parse(type: NewsFeedItem.Type, data: String): NewsFeedData {
        return typeBasedParsers[type]?.parse(data) ?: throw LoveMapException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.InternalServerError
        )
    }
}