package com.lovemap.lovemapbackend.newfeed.dataparser

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.LoveNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.LoveSpotNewsFeedData
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedData
import org.springframework.stereotype.Component

@Component
class LoveNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, LoveNewsFeedData::class.java)
    }
}