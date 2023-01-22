package com.lovemap.lovemapbackend.newsfeed.dataparser

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.newsfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.newsfeed.model.*
import org.springframework.stereotype.Component

interface TypeBasedNewsFeedDataParser {
    fun supportedType(): NewsFeedItem.Type
    fun parse(data: String): NewsFeedData
}

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

@Component
class LoverNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVER
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, LoverNewsFeedData::class.java)
    }
}

@Component
class LoveSpotNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, LoveSpotNewsFeedData::class.java)
    }
}

@Component
class LoveSpotPhotoNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, LoveSpotPhotoNewsFeedData::class.java)
    }
}

@Component
class LoveSpotPhotoLikeNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, PhotoLikeNewsFeedData::class.java)
    }
}

@Component
class LoveSpotReviewNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_REVIEW
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, LoveSpotReviewNewsFeedData::class.java)
    }
}

@Component
class WishlistNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.WISHLIST_ITEM
    }

    override fun parse(data: String): NewsFeedData {
        return objectMapper.readValue(data, WishlistNewsFeedData::class.java)
    }
}



