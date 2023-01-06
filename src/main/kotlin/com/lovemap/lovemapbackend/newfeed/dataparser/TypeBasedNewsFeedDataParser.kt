package com.lovemap.lovemapbackend.newfeed.dataparser

import com.lovemap.lovemapbackend.newfeed.NewsFeedItem
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedData

interface TypeBasedNewsFeedDataParser {
    fun supportedType(): NewsFeedItem.Type
    fun parse(data: String): NewsFeedData
}